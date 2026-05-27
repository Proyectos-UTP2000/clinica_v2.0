import { Component } from '@angular/core';
import { AuthService } from '../../core/services/auth.service';

interface MenuItem {
  label: string;
  route: string;
  permiso?: string;
}

@Component({
  selector: 'app-sidebar',
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.css'
})
export class SidebarComponent {
  menuItems: MenuItem[] = [
    { label: 'Dashboard', route: '/dashboard', permiso: 'dashboard.ver' },
    { label: 'Pacientes', route: '/pacientes', permiso: 'pacientes.ver' },
    { label: 'Medicos', route: '/medicos', permiso: 'medicos.ver' },
    { label: 'Citas', route: '/citas', permiso: 'citas.ver_todas' },
    { label: 'Historial', route: '/historial', permiso: 'historial.ver_todos' },
    { label: 'Pagos', route: '/pagos', permiso: 'pagos.ver' },
    { label: 'Sedes', route: '/sedes', permiso: 'sedes.ver' },
    { label: 'Especialidades', route: '/especialidades', permiso: 'especialidades.ver' }
  ];

  constructor(private readonly authService: AuthService) {}

  puedeVer(item: MenuItem): boolean {
    if (!item.permiso) {
      return true;
    }

    if (item.route === '/citas') {
      return this.authService.hasPermission('citas.ver_todas') || this.authService.hasPermission('citas.ver_propias');
    }

    return this.authService.hasPermission(item.permiso);
  }
}
