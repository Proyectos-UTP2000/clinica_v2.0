import { Component } from '@angular/core';
import { AbstractControl, FormBuilder, ValidationErrors, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';
import { AuthService } from '../../core/services/auth.service';

const PASSWORD_PATTERN = /^(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9])\S{8,}$/;

@Component({
  selector: 'app-cambiar-password',
  templateUrl: './cambiar-password.component.html',
  styleUrl: './cambiar-password.component.css'
})
export class CambiarPasswordComponent {
  cargando = false;
  mensajeError = '';
  mensajeExito = '';

  passwordForm = this.fb.group({
    nuevaPassword: ['', [Validators.required, Validators.pattern(PASSWORD_PATTERN)]],
    repetirPassword: ['', [Validators.required]]
  }, { validators: this.passwordsCoinciden });

  constructor(
    private readonly fb: FormBuilder,
    private readonly authService: AuthService,
    private readonly router: Router
  ) {}

  cambiarPassword(): void {
    this.mensajeError = '';
    this.mensajeExito = '';
    this.passwordForm.markAllAsTouched();

    if (this.passwordForm.invalid) {
      return;
    }

    const request = this.passwordForm.getRawValue();
    this.cargando = true;

    this.authService.cambiarPassword({
      nuevaPassword: request.nuevaPassword ?? '',
      repetirPassword: request.repetirPassword ?? ''
    })
      .pipe(finalize(() => (this.cargando = false)))
      .subscribe({
        next: () => {
          this.authService.marcarPasswordActualizado();
          this.mensajeExito = 'Contrasena actualizada correctamente.';
          this.router.navigateByUrl('/dashboard');
        },
        error: () => {
          this.mensajeError = 'No se pudo actualizar la contrasena. Revise los datos e intente nuevamente.';
        }
      });
  }

  campoInvalido(campo: 'nuevaPassword' | 'repetirPassword'): boolean {
    const control = this.passwordForm.get(campo);
    return Boolean(control?.invalid && (control.dirty || control.touched));
  }

  passwordsDistintas(): boolean {
    return Boolean(this.passwordForm.hasError('passwordsDistintas') && this.passwordForm.touched);
  }

  private passwordsCoinciden(control: AbstractControl): ValidationErrors | null {
    const nuevaPassword = control.get('nuevaPassword')?.value;
    const repetirPassword = control.get('repetirPassword')?.value;
    return nuevaPassword && repetirPassword && nuevaPassword !== repetirPassword
      ? { passwordsDistintas: true }
      : null;
  }
}
