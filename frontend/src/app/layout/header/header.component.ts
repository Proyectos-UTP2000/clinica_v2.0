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

  ngOnInit(): void {}

  get nombreUsuario(): string {
    return this.authService.getNombreUsuario();
  }

  get selectedSedeId(): number | null {
    return this.sesionContextService.selectedSedeId;
  }

  onSedeChange(event: Event): void {
    const value = (event.target as HTMLSelectElement).value;
    const sedeId = value ? Number(value) : null;
    this.sesionContextService.setSede(sedeId);
  }

  cerrarSesion(): void {
    this.authService.logout();
    this.router.navigateByUrl('/login');
  }
}
