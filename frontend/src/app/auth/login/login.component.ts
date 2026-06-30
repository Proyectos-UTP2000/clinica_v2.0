import { Component, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { finalize } from 'rxjs';
import { AuthService } from '../../core/services/auth.service';

@Component({
    selector: 'app-login',
    templateUrl: './login.component.html',
    styleUrl: './login.component.css',
    standalone: false
})
export class LoginComponent implements OnInit {
  cargando = false;
  mensajeError = '';

  loginForm = this.fb.group({
    dni: ['', [Validators.required, Validators.pattern(/^\d{8}$/)]],
    password: ['', [Validators.required]]
  });

  constructor(
    private readonly fb: FormBuilder,
    private readonly authService: AuthService,
    private readonly router: Router,
    private readonly route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      if (params['expirado'] === 'true') {
        this.mensajeError = 'Su sesión ha expirado. Por favor, inicie sesión nuevamente.';
      }
    });
  }

  ingresar(): void {
    this.mensajeError = '';
    this.loginForm.markAllAsTouched();

    if (this.loginForm.invalid) {
      return;
    }

    const { dni, password } = this.loginForm.getRawValue();
    this.cargando = true;

    this.authService.login(dni ?? '', password ?? '')
      .pipe(finalize(() => (this.cargando = false)))
      .subscribe({
        next: (response) => {
          const destino = response.cambioPasswordObligatorio ? '/cambiar-password' : '/dashboard';
          this.router.navigateByUrl(destino);
        },
        error: () => {
          this.mensajeError = 'No se pudo iniciar sesión. Verifique el DNI y la contraseña.';
        }
      });
  }

  campoInvalido(campo: 'dni' | 'password'): boolean {
    const control = this.loginForm.get(campo);
    return Boolean(control?.invalid && (control.dirty || control.touched));
  }
}
