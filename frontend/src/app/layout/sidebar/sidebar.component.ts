import { Component } from '@angular/core';
import { AuthService } from '../../core/services/auth.service';

interface MenuItem {
  label: string;
  route: string;
  permiso?: string;
  code: string;
  group: 'Operacion' | 'Administracion';
}

@Component({
  selector: 'app-sidebar',
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.css'
})
export class SidebarComponent {
  menuItems: MenuItem[] = [
    { label: 'Dashboard', route: '/dashboard', permiso: 'dashboard.ver', code: 'DB', group: 'Operacion' },
    { label: 'Pacientes', route: '/pacientes', permiso: 'pacientes.ver', code: 'PX', group: 'Operacion' },
    { label: 'Medicos', route: '/medicos', permiso: 'medicos.ver', code: 'MD', group: 'Operacion' },
    { label: 'Citas', route: '/citas', permiso: 'citas.ver_todas', code: 'CT', group: 'Operacion' },
    { label: 'Agenda', route: '/agenda', permiso: 'citas.ver_todas', code: 'AG', group: 'Operacion' },
    { label: 'Historial', route: '/historial', permiso: 'historial.ver_todos', code: 'HC', group: 'Operacion' },
    { label: 'Disponibilidad', route: '/disponibilidad', permiso: 'disponibilidad.ver_propia', code: 'DP', group: 'Operacion' },
    { label: 'Pagos', route: '/pagos', permiso: 'pagos.ver', code: 'PG', group: 'Administracion' },
    { label: 'Sedes', route: '/sedes', permiso: 'sedes.ver', code: 'SD', group: 'Administracion' },
    { label: 'Especialidades', route: '/especialidades', permiso: 'especialidades.ver', code: 'ES', group: 'Administracion' }
  ];

  groups: MenuItem['group'][] = ['Operacion', 'Administracion'];

  constructor(private readonly authService: AuthService) {}

  puedeVer(item: MenuItem): boolean {
    if (!item.permiso) {
      return true;
    }

    if (item.route === '/citas' || item.route === '/agenda') {
      return this.authService.hasPermission('citas.ver_todas') || this.authService.hasPermission('citas.ver_propias');
    }

    if (item.route === '/historial') {
      return this.authService.hasPermission('historial.ver_todos')
        || this.authService.hasPermission('historial.ver_propios')
        || this.authService.hasPermission('historial.ver_basico');
    }

    if (item.route === '/disponibilidad') {
      return this.authService.hasPermission('disponibilidad.ver_propia')
        || this.authService.hasPermission('disponibilidad.ver_todas');
    }

    return this.authService.hasPermission(item.permiso);
  }

  visiblesPorGrupo(group: MenuItem['group']): MenuItem[] {
    return this.menuItems.filter((item) => item.group === group && this.puedeVer(item));
  }
}
