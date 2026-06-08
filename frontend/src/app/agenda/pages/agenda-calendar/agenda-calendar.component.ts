import { Component, OnInit } from '@angular/core';
import { finalize, forkJoin } from 'rxjs';
import { AuthService } from '../../../core/services/auth.service';
import { CitaResponse } from '../../../shared/models/cita.model';
import { EspecialidadResponse } from '../../../shared/models/especialidad.model';
import { MedicoResponse } from '../../../shared/models/medico.model';
import { SedeResponse } from '../../../shared/models/sede.model';
import { AgendaService } from '../../services/agenda.service';

type VistaAgenda = 'dia' | 'semana';

@Component({
    selector: 'app-agenda-calendar',
    templateUrl: './agenda-calendar.component.html',
    standalone: false
})
export class AgendaCalendarComponent implements OnInit {
  sedes: SedeResponse[] = [];
  especialidades: EspecialidadResponse[] = [];
  medicos: MedicoResponse[] = [];
  citas: CitaResponse[] = [];
  fechaActual = this.formatearFecha(new Date());
  vista: VistaAgenda = 'dia';
  sedeId: number | null = null;
  especialidadId: number | null = null;
  doctorId: number | null = null;
  cargando = false;
  mensajeError = '';
  citaSeleccionada?: CitaResponse;

  horas = Array.from({ length: 25 }, (_, index) => {
    const totalMinutos = 8 * 60 + index * 30;
    const hora = Math.floor(totalMinutos / 60).toString().padStart(2, '0');
    const minuto = (totalMinutos % 60).toString().padStart(2, '0');
    return `${hora}:${minuto}`;
  });

  constructor(
    public readonly authService: AuthService,
    private readonly agendaService: AgendaService
  ) {}

  ngOnInit(): void {
    this.cargarCatalogos();
  }

  cargarCatalogos(): void {
    this.cargando = true;
    forkJoin({
      sedes: this.agendaService.listarSedes(),
      especialidades: this.agendaService.listarEspecialidades(),
      medicos: this.agendaService.listarMedicos()
    }).pipe(finalize(() => (this.cargando = false))).subscribe({
      next: ({ sedes, especialidades, medicos }) => {
        this.sedes = sedes;
        this.especialidades = especialidades;
        this.medicos = medicos;
        this.cargarAgenda();
      },
      error: () => (this.mensajeError = 'No se pudieron cargar los catalogos de agenda.')
    });
  }

  cargarMedicos(): void {
    this.doctorId = null;
    this.agendaService.listarMedicos({
      sedeId: this.sedeId || undefined,
      especialidadId: this.especialidadId || undefined
    }).subscribe({
      next: (medicos) => (this.medicos = medicos),
      error: () => (this.mensajeError = 'No se pudieron cargar los medicos.')
    });
  }

  cargarAgenda(): void {
    const rango = this.rangoVisible();
    this.cargando = true;
    this.mensajeError = '';
    this.agendaService.listarCitas({
      fechaInicio: rango.inicio,
      fechaFin: rango.fin,
      doctorId: this.doctorId || undefined,
      sedeId: this.sedeId || undefined
    }, !this.authService.hasPermission('citas.ver_todas')).pipe(finalize(() => (this.cargando = false))).subscribe({
      next: (response) => (this.citas = response.content),
      error: () => (this.mensajeError = 'No se pudo cargar la agenda.')
    });
  }

  cambiarFecha(delta: number): void {
    const fecha = new Date(`${this.fechaActual}T00:00:00`);
    fecha.setDate(fecha.getDate() + (this.vista === 'semana' ? delta * 7 : delta));
    this.fechaActual = this.formatearFecha(fecha);
    this.cargarAgenda();
  }

  diasVisibles(): string[] {
    if (this.vista === 'dia') {
      return [this.fechaActual];
    }
    const fecha = new Date(`${this.fechaActual}T00:00:00`);
    const lunes = new Date(fecha);
    lunes.setDate(fecha.getDate() - ((fecha.getDay() + 6) % 7));
    return Array.from({ length: 7 }, (_, index) => {
      const dia = new Date(lunes);
      dia.setDate(lunes.getDate() + index);
      return this.formatearFecha(dia);
    });
  }

  citasEnFranja(dia: string, hora: string): CitaResponse[] {
    return this.citas.filter((cita) => {
      const inicio = new Date(cita.fechaHoraInicio);
      return this.formatearFecha(inicio) === dia && this.formatearHora(inicio) === hora;
    });
  }

  rutaNuevaCita(dia: string, hora: string): unknown[] {
    return ['/citas/crear'];
  }

  queryNuevaCita(dia: string, hora: string): Record<string, string | number> {
    return {
      fechaHoraInicio: `${dia}T${hora}:00`,
      ...(this.doctorId ? { doctorId: this.doctorId } : {}),
      ...(this.sedeId ? { sedeId: this.sedeId } : {})
    };
  }

  private rangoVisible(): { inicio: string; fin: string } {
    const dias = this.diasVisibles();
    return { inicio: dias[0], fin: dias[dias.length - 1] };
  }

  private formatearFecha(fecha: Date): string {
    return fecha.toISOString().slice(0, 10);
  }

  private formatearHora(fecha: Date): string {
    return `${fecha.getHours().toString().padStart(2, '0')}:${fecha.getMinutes().toString().padStart(2, '0')}`;
  }
}
