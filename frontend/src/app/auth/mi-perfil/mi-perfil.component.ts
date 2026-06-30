import { Component, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { finalize } from 'rxjs';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-mi-perfil',
  templateUrl: './mi-perfil.component.html',
  styleUrl: './mi-perfil.component.css',
  standalone: false
})
export class MiPerfilComponent implements OnInit {
  cargando = false;
  guardando = false;
  mensajeError = '';
  mensajeExito = '';

  perfilForm = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    telefono: ['', [Validators.required]],
    passwordActual: [''],
    nuevaPassword: [''],
    repetirPassword: ['']
  });

  constructor(
    private readonly fb: FormBuilder,
    public readonly authService: AuthService
  ) {}

  ngOnInit(): void {
    const usuario = this.authService.getUsuarioAutenticado();
    if (usuario) {
      this.perfilForm.patchValue({
        email: usuario.email || '',
        telefono: usuario.telefono || ''
      });
    }
  }

  actualizar(): void {
    if (this.perfilForm.invalid) {
      this.perfilForm.markAllAsTouched();
      return;
    }

    const val = this.perfilForm.getRawValue();
    
    // Si intenta cambiar password, validamos campos
    if (val.nuevaPassword || val.passwordActual || val.repetirPassword) {
      if (!val.passwordActual) {
        this.mensajeError = 'Debe ingresar su contraseña actual para poder cambiarla.';
        return;
      }
      if (!val.nuevaPassword) {
        this.mensajeError = 'Debe ingresar la nueva contraseña.';
        return;
      }
      if (val.nuevaPassword.length < 8) {
        this.mensajeError = 'La nueva contraseña debe tener al menos 8 caracteres.';
        return;
      }
      if (val.nuevaPassword !== val.repetirPassword) {
        this.mensajeError = 'La nueva contraseña y su confirmación no coinciden.';
        return;
      }
    }

    this.guardando = true;
    this.mensajeError = '';
    this.mensajeExito = '';

    const req: any = {
      email: val.email,
      telefono: val.telefono
    };

    if (val.nuevaPassword) {
      req.passwordActual = val.passwordActual;
      req.nuevaPassword = val.nuevaPassword;
    }

    this.authService.actualizarPerfil(req)
      .pipe(finalize(() => this.guardando = false))
      .subscribe({
        next: () => {
          this.mensajeExito = 'Perfil actualizado correctamente.';
          this.perfilForm.patchValue({
            passwordActual: '',
            nuevaPassword: '',
            repetirPassword: ''
          });
        },
        error: (err) => {
          this.mensajeError = err.error?.mensaje || 'Error al actualizar el perfil.';
        }
      });
  }
}
