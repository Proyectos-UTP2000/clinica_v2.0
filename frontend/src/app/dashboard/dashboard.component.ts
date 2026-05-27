import { Component, OnInit } from '@angular/core';
import { DashboardService } from '../core/services/dashboard.service';
import { DashboardTotalesResponse } from '../shared/models/dashboard-totales.model';

interface MetricCard {
  titulo: string;
  valor: number;
  detalle: string;
}

interface QuickLink {
  label: string;
  route: string;
  description: string;
}

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {
  totales: DashboardTotalesResponse | null = null;
  cargando = true;
  mensajeError = '';

  quickLinks: QuickLink[] = [
    { label: 'Pacientes', route: '/pacientes', description: 'Gestionar registro y contacto' },
    { label: 'Medicos', route: '/medicos', description: 'Consultar personal medico' },
    { label: 'Citas', route: '/citas', description: 'Agendar y revisar atenciones' },
    { label: 'Pagos', route: '/pagos', description: 'Registrar pagos manuales' }
  ];

  constructor(private readonly dashboardService: DashboardService) {}

  ngOnInit(): void {
    this.dashboardService.obtenerTotales().subscribe({
      next: (response) => {
        this.totales = response;
        this.cargando = false;
      },
      error: () => {
        this.mensajeError = 'No se pudieron cargar los totales del dashboard.';
        this.cargando = false;
      }
    });
  }

  get metricas(): MetricCard[] {
    return [
      {
        titulo: 'Pacientes',
        valor: this.totales?.totalPacientes ?? 0,
        detalle: 'Registros activos'
      },
      {
        titulo: 'Medicos',
        valor: this.totales?.totalMedicos ?? 0,
        detalle: 'Profesionales registrados'
      },
      {
        titulo: 'Citas programadas',
        valor: this.totales?.totalCitasProgramadas ?? 0,
        detalle: 'Pendientes de atencion'
      },
      {
        titulo: 'Citas del dia',
        valor: this.totales?.citasHoy ?? 0,
        detalle: 'Agenda de hoy'
      }
    ];
  }
}
