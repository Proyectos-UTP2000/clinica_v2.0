import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Observable } from 'rxjs';
import { AuthService } from '../../core/services/auth.service';
import { SesionContextService } from '../../core/services/sesion-context.service';
import { SedeResponse } from '../../shared/models/sede.model';

@Component({
    selector: 'app-header',
    templateUrl: './header.component.html',
    styleUrl: './header.component.css',
    standalone: false
})
export class HeaderComponent implements OnInit {
  readonly fechaOperativa = new Intl.DateTimeFormat('es-PE', {
    weekday: 'long',
    day: '2-digit',
    month: 'short'
  }).format(new Date());

  sedes$: Observable<SedeResponse[]>;

  constructor(
    private readonly authService: AuthService,
    private readonly router: Router,
    private readonly sesionContextService: SesionContextService
  ) {
    this.sedes$ = this.sesionContextService.sedes$;
  }

  ngOnInit(): void {
    if (this.authService.isLoggedIn()) {
      this.sesionContextService.cargarSedes();
    }
  }

  get nombreUsuario(): string {
    return this.authService.getNombreUsuario();
  }

  get esAdmin(): boolean {
    return this.authService.hasRole('Administrador');
  }

  get selectedSedeId(): number | null {
    return this.sesionContextService.selectedSedeId;
  }

  set selectedSedeId(value: number | null) {
    // When binded via ngModel, string empty is received for 'Todas', map to null
    const val = value === null || value === undefined || String(value) === '' ? null : Number(value);
    this.sesionContextService.setSede(val);
  }

  cerrarSesion(): void {
    this.authService.logout();
    this.router.navigateByUrl('/login');
  }
}
