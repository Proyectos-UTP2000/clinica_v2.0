import { Component, OnInit } from '@angular/core';
import { FormArray, FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { finalize, forkJoin, of, switchMap } from 'rxjs';
import { AuthService } from '../../../core/services/auth.service';
import { SesionContextService } from '../../../core/services/sesion-context.service';
import { EstudioRequest, IndicacionRequest, RecetaRequest } from '../../../shared/models/consulta.model';
import { MedicoResponse } from '../../../shared/models/medico.model';
import { PacienteResponse } from '../../../shared/models/paciente.model';
import { SedeResponse } from '../../../shared/models/sede.model';
import { HistorialService } from '../../services/historial.service';

@Component({
    selector: 'app-crear-consulta',
    templateUrl: './crear-consulta.component.html',
    standalone: false
})
export class CrearConsultaComponent implements OnInit {
  pacientes: PacienteResponse[] = [];
  medicos: MedicoResponse[] = [];
  sedes: SedeResponse[] = [];
  cargando = false;
  guardando = false;
  mensajeError = '';
  adjuntos: File[] = [];

  consultaForm = this.fb.group({
    pacienteId: [null as number | null, Validators.required],
    doctorId: [null as number | null, Validators.required],
    sedeId: [null as number | null, Validators.required],
    citaId: [null as number | null],
    tipo: ['consulta', Validators.required],
    motivoConsulta: ['', Validators.required],
    diagnostico: ['', Validators.required],
    observaciones: [''],
    recetas: this.fb.array([]),
    indicaciones: this.fb.array([]),
    estudios: this.fb.array([])
  });

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly fb: FormBuilder,
    private readonly historialService: HistorialService,
    private readonly authService: AuthService,
    private readonly sesionContextService: SesionContextService
  ) {}

  get recetas(): FormArray {
    return this.consultaForm.get('recetas') as FormArray;
  }

  get indicaciones(): FormArray {
    return this.consultaForm.get('indicaciones') as FormArray;
  }

  get estudios(): FormArray {
    return this.consultaForm.get('estudios') as FormArray;
  }

  ngOnInit(): void {
    const pacienteId = this.route.snapshot.queryParamMap.get('pacienteId');
    const citaId = this.route.snapshot.queryParamMap.get('citaId');
    const doctorId = this.route.snapshot.queryParamMap.get('doctorId');
    const sedeId = this.route.snapshot.queryParamMap.get('sedeId');
    this.consultaForm.patchValue({
      pacienteId: pacienteId ? Number(pacienteId) : null,
      citaId: citaId ? Number(citaId) : null,
      doctorId: doctorId ? Number(doctorId) : null,
      sedeId: sedeId ? Number(sedeId) : null
    });
    this.cargarCatalogos();
  }

  agregarReceta(): void {
    this.recetas.push(this.fb.group({
      medicamento: ['', Validators.required],
      dosis: [''],
      frecuencia: [''],
      duracion: [''],
      indicaciones: ['']
    }));
  }

  agregarIndicacion(): void {
    this.indicaciones.push(this.fb.group({
      tipo: ['reposo', Validators.required],
      descripcion: ['', Validators.required]
    }));
  }

  agregarEstudio(): void {
    this.estudios.push(this.fb.group({
      tipoEstudio: ['', Validators.required],
      detalle: ['', Validators.required]
    }));
  }

  eliminarItem(array: FormArray, index: number): void {
    array.removeAt(index);
  }

  seleccionarAdjuntos(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.adjuntos = Array.from(input.files ?? []);
  }

  quitarAdjunto(index: number): void {
    this.adjuntos = this.adjuntos.filter((_, posicion) => posicion !== index);
  }

  guardar(): void {
    if (this.consultaForm.invalid) {
      this.consultaForm.markAllAsTouched();
      return;
    }
    const value = this.consultaForm.getRawValue();
    this.guardando = true;
    this.mensajeError = '';
    this.historialService.crear({
      pacienteId: Number(value.pacienteId),
      doctorId: Number(value.doctorId),
      sedeId: Number(value.sedeId),
      citaId: value.citaId ? Number(value.citaId) : undefined,
      tipo: value.tipo || 'consulta',
      motivoConsulta: value.motivoConsulta || undefined,
      diagnostico: value.diagnostico || undefined,
      observaciones: value.observaciones || undefined,
      recetas: (value.recetas ?? []) as RecetaRequest[],
      indicaciones: (value.indicaciones ?? []) as IndicacionRequest[],
      estudios: (value.estudios ?? []) as EstudioRequest[]
    }).pipe(
      switchMap((consulta) => {
        if (this.adjuntos.length === 0) {
          return of(consulta);
        }
        return forkJoin(this.adjuntos.map((archivo) => this.historialService.subirAdjunto(consulta.id, archivo)))
          .pipe(switchMap(() => of(consulta)));
      }),
      finalize(() => (this.guardando = false))
    ).subscribe({
      next: (consulta) => this.router.navigate(['/historial/ver', consulta.id]),
      error: () => (this.mensajeError = 'No se pudo registrar la consulta.')
    });
  }

  private cargarCatalogos(): void {
    this.cargando = true;
    this.mensajeError = '';

    const tienePacientesVer = this.authService.hasPermission('pacientes.ver');
    const tieneMedicosVer = this.authService.hasPermission('medicos.ver');

    this.sesionContextService.sedes$.subscribe((sedes) => {
      this.sedes = sedes;
    });

    const calls: any = {};

    if (tienePacientesVer) {
      calls.pacientes = this.historialService.listarPacientes();
    } else {
      const routePacienteId = this.route.snapshot.queryParamMap.get('pacienteId');
      if (routePacienteId) {
        calls.pacienteDetalle = this.historialService.obtenerPacientePorId(Number(routePacienteId));
      } else {
        calls.pacientes = of([]);
      }
    }

    if (tieneMedicosVer) {
      calls.medicos = this.historialService.listarMedicos();
    } else if (this.authService.hasRole('Doctor')) {
      calls.medicoAutenticado = this.historialService.obtenerMedicoAutenticado();
    }

    if (tieneMedicosVer) {
      calls.sedesList = this.historialService.listarSedes();
    } else {
      calls.sedesList = of(this.sedes);
    }

    forkJoin(calls)
      .pipe(finalize(() => (this.cargando = false)))
      .subscribe({
        next: (res: any) => {
          if (res.pacientes) {
            this.pacientes = res.pacientes;
          } else if (res.pacienteDetalle) {
            this.pacientes = [res.pacienteDetalle];
          }

          if (res.medicos) {
            this.medicos = res.medicos;
          } else if (res.medicoAutenticado) {
            this.medicos = [res.medicoAutenticado];
          }

          if (res.sedesList && res.sedesList.length > 0) {
            this.sedes = res.sedesList;
          }

          const currentFormValue = this.consultaForm.value;
          this.consultaForm.patchValue({
            pacienteId: currentFormValue.pacienteId || (this.pacientes.length === 1 ? this.pacientes[0].id : null),
            doctorId: currentFormValue.doctorId || (this.medicos.length === 1 ? this.medicos[0].id : null),
            sedeId: currentFormValue.sedeId || (this.sedes.length === 1 ? this.sedes[0].id : null)
          });
        },
        error: () => (this.mensajeError = 'No se pudieron cargar los catálogos de consulta.')
      });
  }
}
