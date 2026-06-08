import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
    selector: 'app-header',
    templateUrl: './header.component.html',
    styleUrl: './header.component.css',
    standalone: false
})
export class HeaderComponent {
  readonly fechaOperativa = new Intl.DateTimeFormat('es-PE', {
    weekday: 'long',
    day: '2-digit',
    month: 'short'
  }).format(new Date());

  constructor(
    private readonly authService: AuthService,
    private readonly router: Router
  ) {}

  get nombreUsuario(): string {
    return this.authService.getNombreUsuario();
  }

  cerrarSesion(): void {
    this.authService.logout();
    this.router.navigateByUrl('/login');
  }
}
