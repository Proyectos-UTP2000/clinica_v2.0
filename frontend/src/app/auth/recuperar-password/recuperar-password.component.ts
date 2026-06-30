import { Component, OnDestroy } from '@angular/core';
import { FormBuilder, Validators, FormGroup } from '@angular/forms';
import { finalize } from 'rxjs';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

const PASSWORD_PATTERN = /^(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9])\S{8,}$/;

@Component({
    selector: 'app-recuperar-password',
    templateUrl: './recuperar-password.component.html',
    styleUrl: './recuperar-password.component.css',
    standalone: false
})
export class RecuperarPasswordComponent implements OnDestroy {
  pasoActual: 1 | 2 | 3 = 1;
  cargando = false;
  mensajeError = '';
  mensajeExito = '';
  emailSolicitado = '';
  dniSolicitado = '';
  tiempoReenvio = 0;
  private intervalId: any = null;

  solicitudForm = this.fb.group({
    dni: ['', [Validators.required, Validators.pattern(/^\d{8}$/)]],
    email: ['', [Validators.required, Validators.email]]
  });

  codigoForm = this.fb.group({
    codigo: ['', [Validators.required, Validators.minLength(4)]]
  });

  passwordsCoincidenValidator = (group: FormGroup) => {
    const nueva = group.get('nuevaPassword')?.value;
    const repetir = group.get('repetirPassword')?.value;
    return nueva === repetir ? null : { noCoinciden: true };
  };

  passwordForm = this.fb.group({
    nuevaPassword: ['', [Validators.required, Validators.pattern(PASSWORD_PATTERN)]],
    repetirPassword: ['', [Validators.required]]
  }, {
    validators: this.passwordsCoincidenValidator
  });

  constructor(
    private readonly fb: FormBuilder,
    private readonly authService: AuthService,
    private readonly router: Router
  ) {}

  ngOnDestroy(): void {
    this.limpiarCronometro();
  }

  iniciarCronometro(): void {
    this.limpiarCronometro();
    this.tiempoReenvio = 60; // 60 segundos
    this.intervalId = setInterval(() => {
      this.tiempoReenvio--;
      if (this.tiempoReenvio <= 0) {
        this.limpiarCronometro();
      }
    }, 1000);
  }

  limpiarCronometro(): void {
    if (this.intervalId) {
      clearInterval(this.intervalId);
      this.intervalId = null;
    }
  }

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
          this.dniSolicitado = dni ?? '';
          this.emailSolicitado = email ?? '';
          this.pasoActual = 2;
          this.mensajeExito = 'Se envio un codigo de recuperacion al correo registrado.';
          this.iniciarCronometro();
        },
        error: (err) => {
          this.mensajeError = err.error?.mensaje || 'No se pudo generar el código de recuperación.';
        }
      });
  }

  reenviarCodigo(): void {
    if (this.tiempoReenvio > 0 || this.cargando) {
      return;
    }

    this.mensajeError = '';
    this.mensajeExito = '';
    this.cargando = true;

    this.authService.solicitarRecuperacion(this.dniSolicitado, this.emailSolicitado)
      .pipe(finalize(() => (this.cargando = false)))
      .subscribe({
        next: () => {
          this.mensajeExito = 'Código de recuperación reenviado.';
          this.iniciarCronometro();
        },
        error: (err) => {
          this.mensajeError = err.error?.mensaje || 'No se pudo reenviar el código de recuperación.';
        }
      });
  }

  verificarCodigo(): void {
    this.mensajeError = '';
    this.mensajeExito = '';
    this.codigoForm.markAllAsTouched();

    if (this.codigoForm.invalid) {
      return;
    }

    const { codigo } = this.codigoForm.getRawValue();
    this.cargando = true;

    this.authService.validarCodigo(this.emailSolicitado, codigo ?? '')
      .pipe(finalize(() => (this.cargando = false)))
      .subscribe({
        next: () => {
          this.pasoActual = 3;
          this.mensajeExito = 'Código verificado correctamente. Ingrese su nueva contraseña.';
        },
        error: (err) => {
          this.mensajeError = err.error?.mensaje || 'El código no es valido o ya expiro.';
        }
      });
  }

  restablecerPassword(): void {
    this.mensajeError = '';
    this.mensajeExito = '';
    this.passwordForm.markAllAsTouched();

    if (this.passwordForm.invalid) {
      return;
    }

    const { nuevaPassword, repetirPassword } = this.passwordForm.getRawValue();
    const { codigo } = this.codigoForm.getRawValue();
    this.cargando = true;

    this.authService.restablecerPassword(this.emailSolicitado, codigo ?? '', nuevaPassword ?? '', repetirPassword ?? '')
      .pipe(finalize(() => (this.cargando = false)))
      .subscribe({
        next: () => {
          this.mensajeExito = 'Contrasena restablecida con exito. Redirigiendo al login...';
          this.passwordForm.reset();
          this.codigoForm.reset();
          this.solicitudForm.reset();
          this.limpiarCronometro();
          setTimeout(() => {
            this.router.navigate(['/login']);
          }, 3000);
        },
        error: (err) => {
          this.mensajeError = err.error?.mensaje || 'No se pudo restablecer la contraseña. El código puede haber expirado.';
        }
      });
  }

  campoSolicitudInvalido(campo: 'dni' | 'email'): boolean {
    const control = this.solicitudForm.get(campo);
    return Boolean(control?.invalid && (control.dirty || control.touched));
  }

  campoCodigoInvalido(campo: 'codigo'): boolean {
    const control = this.codigoForm.get(campo);
    return Boolean(control?.invalid && (control.dirty || control.touched));
  }

  campoPasswordInvalido(campo: 'nuevaPassword' | 'repetirPassword'): boolean {
    const control = this.passwordForm.get(campo);
    return Boolean(control?.invalid && (control.dirty || control.touched));
  }
}
