import { Component, OnInit } from '@angular/core';
import { AbstractControl, FormBuilder, ValidationErrors, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { finalize, forkJoin } from 'rxjs';
import { EspecialidadResponse } from '../../../shared/models/especialidad.model';
import { MedicoCreateRequest } from '../../../shared/models/medico.model';
import { SedeResponse } from '../../../shared/models/sede.model';
import { ConsultorioResponse } from '../../../shared/models/consultorio.model';
import { MedicoService } from '../../services/medico.service';
import { ConsultorioService } from '../../../consultorios/services/consultorio.service';
import { ApiErrorService } from '../../../core/services/api-error.service';

@Component({
    selector: 'app-crear-medico',
    templateUrl: './crear-medico.component.html',
    styleUrl: './crear-medico.component.css',
    standalone: false
})
export class CrearMedicoComponent implements OnInit {
  todasEspecialidades: EspecialidadResponse[] = [];
  especialidades: EspecialidadResponse[] = [];
  subespecialidades: EspecialidadResponse[] = [];
  sedes: SedeResponse[] = [];
  consultorios: ConsultorioResponse[] = [];
  cargandoCatalogos = false;
  consultandoDni = false;
  guardando = false;
  mensajeError = '';
  mensajeInfo = '';

  medicoForm = this.fb.group({
    dni: ['', [Validators.required, Validators.pattern(/^\d{8}$/)]],
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
    private readonly medicoService: MedicoService,
    private readonly consultorioService: ConsultorioService,
    private readonly apiErrorService: ApiErrorService,
    private readonly router: Router
  ) {}

  ngOnInit(): void {
    this.cargarCatalogos();
    this.medicoForm.get('especialidadId')?.valueChanges.subscribe((value) => {
      this.subespecialidades = this.obtenerSubespecialidades(Number(value));
      this.medicoForm.patchValue({ subespecialidadId: '' }, { emitEvent: false });
    });
  }

  guardar(): void {
    this.mensajeError = '';
    this.mensajeInfo = '';
    this.medicoForm.markAllAsTouched();

    if (this.medicoForm.invalid) {
      return;
    }

    this.guardando = true;
    this.medicoService.crear(this.construirRequest())
      .pipe(finalize(() => (this.guardando = false)))
      .subscribe({
        next: () => this.router.navigateByUrl('/medicos'),
        error: (err) => {
          this.mensajeError = this.apiErrorService.obtenerMensajeError(err);
        }
      });
  }

  consultarDni(): void {
    this.mensajeError = '';
    this.mensajeInfo = '';
    const dniControl = this.medicoForm.get('dni');
    dniControl?.markAsTouched();

    if (dniControl?.invalid) {
      return;
    }

    const dni = dniControl?.value ?? '';
    this.consultandoDni = true;
    this.medicoService.consultarDni(dni)
      .pipe(finalize(() => (this.consultandoDni = false)))
      .subscribe({
        next: (info) => {
          this.medicoForm.patchValue({ nombres: info.nombres, apellidos: info.apellidos });
          this.mensajeInfo = 'Datos encontrados por DNI. Revise y complete los campos restantes.';
        },
        error: () => {
          this.mensajeError = 'No se pudieron consultar los datos del DNI.';
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

  private cargarCatalogos(): void {
    this.cargandoCatalogos = true;
    forkJoin({
      especialidades: this.medicoService.getEspecialidades(),
      sedes: this.medicoService.getSedes(),
      consultorios: this.consultorioService.listar(0, 100)
    })
      .pipe(finalize(() => (this.cargandoCatalogos = false)))
      .subscribe({
        next: ({ especialidades, sedes, consultorios }) => {
          this.todasEspecialidades = especialidades;
          this.especialidades = especialidades.filter((item) => !item.especialidadPadreId);
          this.sedes = sedes;
          this.consultorios = consultorios.content;
        },
        error: () => {
          this.mensajeError = 'No se pudieron cargar especialidades, sedes o consultorios.';
        }
      });
  }

  private obtenerSubespecialidades(especialidadId: number): EspecialidadResponse[] {
    return this.todasEspecialidades.filter((item) => item.especialidadPadreId === especialidadId);
  }

  private construirRequest(): MedicoCreateRequest {
    const raw = this.medicoForm.getRawValue();
    return {
      dni: raw.dni ?? '',
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
