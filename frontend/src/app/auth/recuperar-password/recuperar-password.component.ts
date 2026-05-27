import { Component } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { finalize } from 'rxjs';
import { AuthService } from '../../core/services/auth.service';

const PASSWORD_PATTERN = /^(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9])\S{8,}$/;

@Component({
  selector: 'app-recuperar-password',
  templateUrl: './recuperar-password.component.html',
  styleUrl: './recuperar-password.component.css'
})
export class RecuperarPasswordComponent {
  pasoActual: 1 | 2 = 1;
  cargando = false;
  mensajeError = '';
  mensajeExito = '';
  emailSolicitado = '';

  solicitudForm = this.fb.group({
    dni: ['', [Validators.required, Validators.pattern(/^\d{8}$/)]],
    email: ['', [Validators.required, Validators.email]]
  });

  codigoForm = this.fb.group({
    codigo: ['', [Validators.required, Validators.minLength(4)]],
    nuevaPassword: ['', [Validators.required, Validators.pattern(PASSWORD_PATTERN)]]
  });

  constructor(
    private readonly fb: FormBuilder,
    private readonly authService: AuthService
  ) {}

  solicitarCodigo(): void {
    this.mensajeError = '';
    this.mensajeExito = '';
    this.solicitudForm.markAllAsTouched();

    if (this.solicitudForm.invalid) {
      return;
    }

    const { dni, email } = this.solicitudForm.getRawValue();
    this.cargando = true;

    this.authService.solicitarRecuperacion(dni ?? '', email ?? '')
      .pipe(finalize(() => (this.cargando = false)))
      .subscribe({
        next: () => {
          this.emailSolicitado = email ?? '';
          this.pasoActual = 2;
          this.mensajeExito = 'Se envio un codigo de recuperacion al correo registrado.';
        },
        error: () => {
          this.mensajeError = 'No se pudo generar el codigo de recuperacion.';
        }
      });
  }

  restablecerPassword(): void {
    this.mensajeError = '';
    this.mensajeExito = '';
    this.codigoForm.markAllAsTouched();

    if (this.codigoForm.invalid) {
      return;
    }

    const { codigo, nuevaPassword } = this.codigoForm.getRawValue();
    this.cargando = true;

    this.authService.restablecerPassword(this.emailSolicitado, codigo ?? '', nuevaPassword ?? '')
      .pipe(finalize(() => (this.cargando = false)))
      .subscribe({
        next: () => {
          this.mensajeExito = 'Contrasena restablecida. Ya puede iniciar sesion.';
          this.codigoForm.reset();
        },
        error: () => {
          this.mensajeError = 'El codigo no es valido o la solicitud expiro.';
        }
      });
  }

  campoSolicitudInvalido(campo: 'dni' | 'email'): boolean {
    const control = this.solicitudForm.get(campo);
    return Boolean(control?.invalid && (control.dirty || control.touched));
  }

  campoCodigoInvalido(campo: 'codigo' | 'nuevaPassword'): boolean {
    const control = this.codigoForm.get(campo);
    return Boolean(control?.invalid && (control.dirty || control.touched));
  }
}
