import { Component } from '@angular/core';

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
    { label: 'Pacientes', route: '/pacientes' },
    { label: 'Medicos', route: '/medicos' },
    { label: 'Citas', route: '/citas' },
    { label: 'Historial', route: '/historial' },
    { label: 'Pagos', route: '/pagos' },
    { label: 'Sedes', route: '/sedes' },
    { label: 'Especialidades', route: '/especialidades' }
  ];
}
