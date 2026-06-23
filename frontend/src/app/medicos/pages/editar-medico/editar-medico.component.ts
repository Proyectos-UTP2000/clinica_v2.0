import { Component, OnInit } from '@angular/core';
import { AbstractControl, FormBuilder, ValidationErrors, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { finalize, forkJoin } from 'rxjs';
import { EspecialidadResponse } from '../../../shared/models/especialidad.model';
import { MedicoResponse, MedicoUpdateRequest } from '../../../shared/models/medico.model';
import { SedeResponse } from '../../../shared/models/sede.model';
import { ConsultorioResponse } from '../../../shared/models/consultorio.model';
import { MedicoService } from '../../services/medico.service';
import { ConsultorioService } from '../../../consultorios/services/consultorio.service';
import { ApiErrorService } from '../../../core/services/api-error.service';

@Component({
    selector: 'app-editar-medico',
    templateUrl: './editar-medico.component.html',
    styleUrl: './editar-medico.component.css',
    standalone: false
})
export class EditarMedicoComponent implements OnInit {
  medico: MedicoResponse | null = null;
  todasEspecialidades: EspecialidadResponse[] = [];
  especialidades: EspecialidadResponse[] = [];
  subespecialidades: EspecialidadResponse[] = [];
  sedes: SedeResponse[] = [];
  consultorios: ConsultorioResponse[] = [];
  cargando = false;
  guardando = false;
  mensajeError = '';

  medicoForm = this.fb.group({
    nombres: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
    apellidos: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(100)]],
    email: ['', [Validators.required, Validators.email]],
    telefono: ['', [Validators.pattern(/^\d{7,15}$/)]],
    fechaNacimiento: [''],
    especialidadId: ['', [Validators.required]],
    subespecialidadId: [''],
    sedesIds: [[] as number[], [this.alMenosUnaSede]],
    consultorioIds: [[] as number[]]
  });

  constructor(
    private readonly fb: FormBuilder,
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly medicoService: MedicoService,
    private readonly consultorioService: ConsultorioService,
    private readonly apiErrorService: ApiErrorService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.cargarDatos(id);
    this.medicoForm.get('especialidadId')?.valueChanges.subscribe((value) => {
      this.subespecialidades = this.obtenerSubespecialidades(Number(value));
      this.medicoForm.patchValue({ subespecialidadId: '' }, { emitEvent: false });
    });
  }

  guardar(): void {
    if (!this.medico) {
      return;
    }
    this.mensajeError = '';
    this.medicoForm.markAllAsTouched();
    if (this.medicoForm.invalid) {
      return;
    }

    this.guardando = true;
    this.medicoService.actualizar(this.medico.id, this.construirRequest())
      .pipe(finalize(() => (this.guardando = false)))
      .subscribe({
        next: () => this.router.navigateByUrl('/medicos'),
        error: (err) => {
          this.mensajeError = this.apiErrorService.obtenerMensajeError(err);
        }
      });
  }

  toggleSede(sedeId: number, checked: boolean): void {
    const control = this.medicoForm.get('sedesIds');
    const actuales = [...((control?.value as number[]) ?? [])];
    const siguiente = checked ? [...actuales, sedeId] : actuales.filter((id) => id !== sedeId);
    control?.setValue(siguiente);
    control?.markAsTouched();
  }

  sedeSeleccionada(sedeId: number): boolean {
    return ((this.medicoForm.get('sedesIds')?.value as number[]) ?? []).includes(sedeId);
  }

  toggleConsultorio(consultorioId: number, checked: boolean): void {
    const control = this.medicoForm.get('consultorioIds');
    const actuales = [...((control?.value as number[]) ?? [])];
    const siguiente = checked ? [...actuales, consultorioId] : actuales.filter((id) => id !== consultorioId);
    control?.setValue(siguiente);
    control?.markAsTouched();
  }

  consultorioSeleccionado(consultorioId: number): boolean {
    return ((this.medicoForm.get('consultorioIds')?.value as number[]) ?? []).includes(consultorioId);
  }

  get consultoriosAgrupadosPorSede(): { sedeNombre: string, list: ConsultorioResponse[] }[] {
    const sedesSeleccionadas = (this.medicoForm.get('sedesIds')?.value as number[]) ?? [];
    const agrupados: { [key: number]: { name: string, list: ConsultorioResponse[] } } = {};
    
    this.consultorios.forEach(c => {
      if (sedesSeleccionadas.includes(c.sedeId)) {
        if (!agrupados[c.sedeId]) {
          agrupados[c.sedeId] = { name: c.sedeNombre, list: [] };
        }
        agrupados[c.sedeId].list.push(c);
      }
    });
    
    return Object.values(agrupados).map(item => ({
      sedeNombre: item.name,
      list: item.list
    }));
  }

  campoInvalido(campo: string): boolean {
    const control = this.medicoForm.get(campo);
    return Boolean(control?.invalid && (control.dirty || control.touched));
  }

  private cargarDatos(id: number): void {
    this.cargando = true;
    forkJoin({
      medico: this.medicoService.obtenerPorId(id),
      especialidades: this.medicoService.getEspecialidades(),
      sedes: this.medicoService.getSedes(),
      consultorios: this.consultorioService.listar(0, 100)
    })
      .pipe(finalize(() => (this.cargando = false)))
      .subscribe({
        next: ({ medico, especialidades, sedes, consultorios }) => {
          this.medico = medico;
          this.todasEspecialidades = especialidades;
          this.especialidades = especialidades.filter((item) => !item.especialidadPadreId);
          this.sedes = sedes;
          this.consultorios = consultorios.content;
          this.precargarFormulario(medico);
        },
        error: () => {
          this.mensajeError = 'No se pudo cargar el medico solicitado.';
        }
      });
  }

  private precargarFormulario(medico: MedicoResponse): void {
    const especialidad = this.especialidades.find((item) => item.nombre === medico.especialidadNombre);
    this.subespecialidades = this.obtenerSubespecialidades(especialidad?.id ?? 0);
    const subespecialidad = this.subespecialidades.find((item) => item.nombre === medico.subespecialidadNombre);
    const sedesIds = this.sedes.filter((sede) => medico.sedes.includes(sede.nombre)).map((sede) => sede.id);
    const consultorioIds = medico.consultorioIds || [];
 
    this.medicoForm.patchValue({
      nombres: medico.nombres,
      apellidos: medico.apellidos,
      email: medico.email,
      telefono: medico.telefono ?? '',
      fechaNacimiento: medico.fechaNacimiento ?? '',
      especialidadId: especialidad ? String(especialidad.id) : '',
      subespecialidadId: subespecialidad ? String(subespecialidad.id) : '',
      sedesIds,
      consultorioIds
    }, { emitEvent: false });
  }

  private obtenerSubespecialidades(especialidadId: number): EspecialidadResponse[] {
    return this.todasEspecialidades.filter((item) => item.especialidadPadreId === especialidadId);
  }

  private construirRequest(): MedicoUpdateRequest {
    const raw = this.medicoForm.getRawValue();
    return {
      nombres: raw.nombres ?? '',
      apellidos: raw.apellidos ?? '',
      email: raw.email ?? '',
      telefono: raw.telefono || undefined,
      fechaNacimiento: raw.fechaNacimiento || undefined,
      especialidadId: Number(raw.especialidadId),
      subespecialidadId: raw.subespecialidadId ? Number(raw.subespecialidadId) : undefined,
      sedesIds: raw.sedesIds ?? [],
      consultorioIds: raw.consultorioIds ?? []
    };
  }

  private alMenosUnaSede(control: AbstractControl): ValidationErrors | null {
    const value = control.value as number[] | null;
    return value && value.length > 0 ? null : { sedesRequeridas: true };
  }
}
