import { Component, OnInit } from '@angular/core';
import { AbstractControl, FormBuilder, ValidationErrors, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { finalize, forkJoin } from 'rxjs';
import { EspecialidadResponse } from '../../../shared/models/especialidad.model';
import { MedicoCreateRequest } from '../../../shared/models/medico.model';
import { SedeResponse } from '../../../shared/models/sede.model';
import { MedicoService } from '../../services/medico.service';

@Component({
  selector: 'app-crear-medico',
  templateUrl: './crear-medico.component.html',
  styleUrl: './crear-medico.component.css'
})
export class CrearMedicoComponent implements OnInit {
  todasEspecialidades: EspecialidadResponse[] = [];
  especialidades: EspecialidadResponse[] = [];
  subespecialidades: EspecialidadResponse[] = [];
  sedes: SedeResponse[] = [];
  cargandoCatalogos = false;
  guardando = false;
  mensajeError = '';

  medicoForm = this.fb.group({
    dni: ['', [Validators.required, Validators.pattern(/^\d{8}$/)]],
    nombres: ['', [Validators.required, Validators.maxLength(80)]],
    apellidos: ['', [Validators.required, Validators.maxLength(80)]],
    email: ['', [Validators.required, Validators.email]],
    telefono: ['', [Validators.pattern(/^\d{7,15}$/)]],
    fechaNacimiento: [''],
    especialidadId: ['', [Validators.required]],
    subespecialidadId: [''],
    sedesIds: [[] as number[], [this.alMenosUnaSede]]
  });

  constructor(
    private readonly fb: FormBuilder,
    private readonly medicoService: MedicoService,
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
    this.medicoForm.markAllAsTouched();

    if (this.medicoForm.invalid) {
      return;
    }

    this.guardando = true;
    this.medicoService.crear(this.construirRequest())
      .pipe(finalize(() => (this.guardando = false)))
      .subscribe({
        next: () => this.router.navigateByUrl('/medicos'),
        error: () => {
          this.mensajeError = 'No se pudo crear el medico. Revise si el DNI o email ya existen.';
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

  campoInvalido(campo: string): boolean {
    const control = this.medicoForm.get(campo);
    return Boolean(control?.invalid && (control.dirty || control.touched));
  }

  private cargarCatalogos(): void {
    this.cargandoCatalogos = true;
    forkJoin({
      especialidades: this.medicoService.getEspecialidades(),
      sedes: this.medicoService.getSedes()
    })
      .pipe(finalize(() => (this.cargandoCatalogos = false)))
      .subscribe({
        next: ({ especialidades, sedes }) => {
          this.todasEspecialidades = especialidades;
          this.especialidades = especialidades.filter((item) => !item.especialidadPadreId);
          this.sedes = sedes;
        },
        error: () => {
          this.mensajeError = 'No se pudieron cargar especialidades o sedes.';
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
      sedesIds: raw.sedesIds ?? []
    };
  }

  private alMenosUnaSede(control: AbstractControl): ValidationErrors | null {
    const value = control.value as number[] | null;
    return value && value.length > 0 ? null : { sedesRequeridas: true };
  }
}
