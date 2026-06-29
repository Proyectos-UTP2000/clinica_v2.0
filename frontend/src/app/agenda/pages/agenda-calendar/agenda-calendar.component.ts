import { Component, OnInit } from '@angular/core';
import { finalize, forkJoin } from 'rxjs';
import { AuthService } from '../../../core/services/auth.service';
import { SesionContextService } from '../../../core/services/sesion-context.service';
import { CitaResponse } from '../../../shared/models/cita.model';
import { EspecialidadResponse } from '../../../shared/models/especialidad.model';
import { MedicoResponse } from '../../../shared/models/medico.model';
import { SedeResponse } from '../../../shared/models/sede.model';
import { AgendaService } from '../../services/agenda.service';

type VistaAgenda = 'dia' | 'semana' | 'mes';

@Component({
    selector: 'app-agenda-calendar',
    templateUrl: './agenda-calendar.component.html',
    styleUrl: './agenda-calendar.component.css',
    standalone: false
})
export class AgendaCalendarComponent implements OnInit {
  sedes: SedeResponse[] = [];
  especialidades: EspecialidadResponse[] = [];
  medicos: MedicoResponse[] = [];
  citas: CitaResponse[] = [];
  fechaActual = this.formatearFecha(new Date());
  vista: VistaAgenda = 'semana';
  sedeId: number | null = null;
  especialidadId: number | null = null;
  doctorId: number | null = null;
  cargando = false;
  mensajeError = '';
  citaSeleccionada?: CitaResponse;
  busquedaMedico = '';
  mostrarDropdownMedico = false;

  horas = Array.from({ length: 25 }, (_, index) => {
    const totalMinutos = 8 * 60 + index * 30;
    const hora = Math.floor(totalMinutos / 60).toString().padStart(2, '0');
    const minuto = (totalMinutos % 60).toString().padStart(2, '0');
    return `${hora}:${minuto}`;
  });

  constructor(
    public readonly authService: AuthService,
    private readonly agendaService: AgendaService,
    private readonly sesionContextService: SesionContextService
  ) {}

  ngOnInit(): void {
    this.sesionContextService.sedes$.subscribe((sedes) => {
      this.sedes = sedes;
    });
    this.cargarCatalogos();
    this.sesionContextService.selectedSedeId$.subscribe((sedeId) => {
      this.sedeId = sedeId;
      this.cargarMedicos();
      this.cargarAgenda();
    });
  }

  cargarCatalogos(): void {
    this.cargando = true;
    const calls: any = {};
    if (this.authService.hasPermission('citas.ver_todas')) {
      calls.especialidades = this.agendaService.listarEspecialidades();
      calls.medicos = this.agendaService.listarMedicos();
    }

    if (Object.keys(calls).length === 0) {
      this.cargando = false;
      this.cargarAgenda();
      return;
    }

    forkJoin(calls).pipe(finalize(() => (this.cargando = false))).subscribe({
      next: (res: any) => {
        if (res.especialidades) {
          this.especialidades = res.especialidades;
        }
        if (res.medicos) {
          this.medicos = res.medicos;
        }
        this.cargarAgenda();
      },
      error: () => (this.mensajeError = 'No se pudieron cargar los catalogos de agenda.')
    });
  }

  cargarMedicos(): void {
    if (!this.authService.hasPermission('citas.ver_todas')) {
      return;
    }
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
    if (this.vista === 'mes') {
      fecha.setMonth(fecha.getMonth() + delta);
    } else if (this.vista === 'semana') {
      fecha.setDate(fecha.getDate() + delta * 7);
    } else {
      fecha.setDate(fecha.getDate() + delta);
    }
    this.fechaActual = this.formatearFecha(fecha);
    this.cargarAgenda();
  }

  diasVisibles(): string[] {
    if (this.vista === 'dia') {
      return [this.fechaActual];
    }
    if (this.vista === 'semana') {
      const fecha = new Date(`${this.fechaActual}T00:00:00`);
      const lunes = new Date(fecha);
      lunes.setDate(fecha.getDate() - ((fecha.getDay() + 6) % 7));
      return Array.from({ length: 7 }, (_, index) => {
        const dia = new Date(lunes);
        dia.setDate(lunes.getDate() + index);
        return this.formatearFecha(dia);
      });
    }
    // vista === 'mes'
    const date = new Date(`${this.fechaActual}T00:00:00`);
    const startOfMonth = new Date(date.getFullYear(), date.getMonth(), 1);
    const startMonday = new Date(startOfMonth);
    const day = startOfMonth.getDay();
    const diff = day === 0 ? 6 : day - 1;
    startMonday.setDate(startOfMonth.getDate() - diff);

    return Array.from({ length: 42 }, (_, index) => {
      const dia = new Date(startMonday);
      dia.setDate(startMonday.getDate() + index);
      return this.formatearFecha(dia);
    });
  }

  citasEnDia(dia: string): CitaResponse[] {
    return this.citas.filter((cita) => {
      const inicio = new Date(cita.fechaHoraInicio);
      return this.formatearFecha(inicio) === dia;
    });
  }

  esMismoMes(dia: string): boolean {
    const date = new Date(`${dia}T00:00:00`);
    const current = new Date(`${this.fechaActual}T00:00:00`);
    return date.getMonth() === current.getMonth();
  }

  seleccionarDia(dia: string): void {
    this.fechaActual = dia;
    this.vista = 'dia';
    this.cargarAgenda();
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
  get textoMedicoSeleccionado(): string {
    if (!this.doctorId) return 'Todos';
    const medico = this.medicos.find(m => m.id === this.doctorId);
    return medico ? `${medico.nombres} ${medico.apellidos}` : 'Todos';
  }

  medicosFiltradosBusqueda(): MedicoResponse[] {
    const query = this.busquedaMedico.toLowerCase().trim();
    if (!query) {
      return this.medicos;
    }
    return this.medicos.filter(m => 
      `${m.nombres} ${m.apellidos}`.toLowerCase().includes(query)
    );
  }

  seleccionarMedico(medico: MedicoResponse | null): void {
    if (medico) {
      this.doctorId = medico.id;
      this.busquedaMedico = `${medico.nombres} ${medico.apellidos}`;
    } else {
      this.doctorId = null;
      this.busquedaMedico = '';
    }
    this.mostrarDropdownMedico = false;
    this.cargarAgenda();
  }

  onFocusMedico(): void {
    this.mostrarDropdownMedico = true;
    this.busquedaMedico = '';
  }

  onBlurMedico(): void {
    setTimeout(() => {
      this.mostrarDropdownMedico = false;
      this.busquedaMedico = this.textoMedicoSeleccionado;
    }, 200);
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
