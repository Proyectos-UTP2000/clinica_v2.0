import { Component, OnInit } from '@angular/core';
import { FormArray, FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { finalize, forkJoin } from 'rxjs';
import { EstudioRequest, IndicacionRequest, RecetaRequest } from '../../../shared/models/consulta.model';
import { MedicoResponse } from '../../../shared/models/medico.model';
import { PacienteResponse } from '../../../shared/models/paciente.model';
import { SedeResponse } from '../../../shared/models/sede.model';
import { HistorialService } from '../../services/historial.service';

@Component({
  selector: 'app-crear-consulta',
  templateUrl: './crear-consulta.component.html'
})
export class CrearConsultaComponent implements OnInit {
  pacientes: PacienteResponse[] = [];
  medicos: MedicoResponse[] = [];
  sedes: SedeResponse[] = [];
  cargando = false;
  guardando = false;
  mensajeError = '';

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
    private readonly historialService: HistorialService
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
    this.consultaForm.patchValue({
      pacienteId: pacienteId ? Number(pacienteId) : null,
      citaId: citaId ? Number(citaId) : null
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
    }).pipe(finalize(() => (this.guardando = false))).subscribe({
      next: (consulta) => this.router.navigate(['/historial/ver', consulta.id]),
      error: () => (this.mensajeError = 'No se pudo registrar la consulta.')
    });
  }

  private cargarCatalogos(): void {
    this.cargando = true;
    forkJoin({
      pacientes: this.historialService.listarPacientes(),
      medicos: this.historialService.listarMedicos(),
      sedes: this.historialService.listarSedes()
    }).pipe(finalize(() => (this.cargando = false))).subscribe({
      next: ({ pacientes, medicos, sedes }) => {
        this.pacientes = pacientes;
        this.medicos = medicos;
        this.sedes = sedes;
      },
      error: () => (this.mensajeError = 'No se pudieron cargar los catalogos.')
    });
  }
}
