import { Component } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';
import { PacienteCreateRequest } from '../../../shared/models/paciente.model';
import { PacienteService } from '../../services/paciente.service';

@Component({
    selector: 'app-crear-paciente',
    templateUrl: './crear-paciente.component.html',
    styleUrl: './crear-paciente.component.css',
    standalone: false
})
export class CrearPacienteComponent {
  cargando = false;
  consultandoDni = false;
  mensajeError = '';
  mensajeInfo = '';

  pacienteForm = this.fb.group({
    dni: ['', [Validators.required, Validators.pattern(/^\d{8}$/)]],
    nombres: ['', [Validators.required, Validators.maxLength(80)]],
    apellidos: ['', [Validators.required, Validators.maxLength(80)]],
    sexo: [''],
    fechaNacimiento: ['', [Validators.required]],
    telefono: ['', [Validators.required, Validators.pattern(/^\d{7,15}$/)]],
    email: ['', [Validators.email]]
  });

  constructor(
    private readonly fb: FormBuilder,
    private readonly pacienteService: PacienteService,
    private readonly router: Router
  ) {}

  guardar(): void {
    this.mensajeError = '';
    this.mensajeInfo = '';
    this.pacienteForm.markAllAsTouched();

    if (this.pacienteForm.invalid) {
      return;
    }

    this.cargando = true;
    this.pacienteService.crear(this.construirRequest())
      .pipe(finalize(() => (this.cargando = false)))
      .subscribe({
        next: () => this.router.navigateByUrl('/pacientes'),
        error: () => {
          this.mensajeError = 'No se pudo crear el paciente. Revise si el DNI o email ya existen.';
        }
      });
  }

  consultarDni(): void {
    this.mensajeError = '';
    this.mensajeInfo = '';
    const dniControl = this.pacienteForm.get('dni');
    dniControl?.markAsTouched();

    if (dniControl?.invalid) {
      return;
    }

    const dni = dniControl?.value ?? '';
    this.consultandoDni = true;
    this.pacienteService.consultarDni(dni)
      .pipe(finalize(() => (this.consultandoDni = false)))
      .subscribe({
        next: (info) => {
          this.pacienteForm.patchValue({ nombres: info.nombres, apellidos: info.apellidos });
          this.mensajeInfo = 'Datos encontrados por DNI. Revise y complete los campos restantes.';
        },
        error: () => {
          this.mensajeError = 'No se pudieron consultar los datos del DNI.';
        }
      });
  }

  campoInvalido(campo: string): boolean {
    const control = this.pacienteForm.get(campo);
    return Boolean(control?.invalid && (control.dirty || control.touched));
  }

  private construirRequest(): PacienteCreateRequest {
    const raw = this.pacienteForm.getRawValue();
    return {
      dni: raw.dni ?? '',
      nombres: raw.nombres ?? '',
      apellidos: raw.apellidos ?? '',
      sexo: raw.sexo || undefined,
      fechaNacimiento: raw.fechaNacimiento ?? '',
      telefono: raw.telefono ?? '',
      email: raw.email || undefined
    };
  }
}
