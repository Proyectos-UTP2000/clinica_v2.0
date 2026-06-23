import { Component, OnInit } from '@angular/core';
import { FormBuilder } from '@angular/forms';
import { finalize, forkJoin } from 'rxjs';
import { AuthService } from '../../../core/services/auth.service';
import { SesionContextService } from '../../../core/services/sesion-context.service';
import { CitaResponse } from '../../../shared/models/cita.model';
import { MedicoResponse } from '../../../shared/models/medico.model';
import { PacienteResponse } from '../../../shared/models/paciente.model';
import { Page } from '../../../shared/models/page.model';
import { CitaService } from '../../services/cita.service';

@Component({
    selector: 'app-listar-citas',
    templateUrl: './listar-citas.component.html',
    styleUrl: './listar-citas.component.css',
    standalone: false
})
export class ListarCitasComponent implements OnInit {
  citas: CitaResponse[] = [];
  pacientes: PacienteResponse[] = [];
  medicos: MedicoResponse[] = [];
  page = 0;
  size = 10;
  totalPages = 0;
  totalElements = 0;
  cargando = false;
  procesando = false;
  mensajeError = '';
  mensajeExito = '';
  citaCancelar: CitaResponse | null = null;
  citaReprogramar: CitaResponse | null = null;

  filtrosForm = this.fb.group({
    pacienteId: [''],
    doctorId: [''],
    fecha: ['']
  });

  reprogramarForm = this.fb.group({
    nuevaFechaHora: [''],
    doctorId: ['']
  });

  constructor(
    public readonly authService: AuthService,
    private readonly fb: FormBuilder,
    private readonly citaService: CitaService,
    private readonly sesionContextService: SesionContextService
  ) {}

  ngOnInit(): void {
    forkJoin({
      pacientes: this.citaService.listarPacientes(),
      medicos: this.citaService.listarMedicos()
    }).subscribe({
      next: ({ pacientes, medicos }) => {
        this.pacientes = pacientes;
        this.medicos = medicos;
      }
    });

    this.sesionContextService.selectedSedeId$.subscribe(() => {
      this.cargarCitas(0);
    });
  }

  get usaAgendaPropia(): boolean {
    return this.authService.hasPermission('citas.ver_propias') && !this.authService.hasPermission('citas.ver_todas');
  }

  cargarCitas(page = this.page): void {
    this.cargando = true;
    this.mensajeError = '';
    const filtros = this.obtenerFiltros();
    const request$ = this.usaAgendaPropia
      ? this.citaService.listarPropias(filtros.fecha, page, this.size)
      : this.citaService.listar(filtros, page, this.size);

    request$
      .pipe(finalize(() => (this.cargando = false)))
      .subscribe({
        next: (response) => this.aplicarPagina(response),
        error: () => {
          this.mensajeError = 'No se pudo cargar la lista de citas.';
        }
      });
  }

  buscar(): void {
    this.cargarCitas(0);
  }

  limpiarFiltros(): void {
    this.filtrosForm.reset({ pacienteId: '', doctorId: '', fecha: '' });
    this.cargarCitas(0);
  }

  abrirCancelacion(cita: CitaResponse): void {
    this.citaCancelar = cita;
  }

  cancelarCita(): void {
    if (!this.citaCancelar) {
      return;
    }
    this.procesando = true;
    this.citaService.cancelar(this.citaCancelar.id)
      .pipe(finalize(() => (this.procesando = false)))
      .subscribe({
        next: () => {
          this.mensajeExito = 'Cita cancelada correctamente.';
          this.citaCancelar = null;
          this.cargarCitas();
        },
        error: () => {
          this.mensajeError = 'No se pudo cancelar la cita.';
        }
      });
  }

  puedeReprogramar(cita: CitaResponse): boolean {
    if (cita.estado === 'no_asistida' && !cita.pagoAnticipado) {
      return false;
    }
    return true;
  }

  abrirReprogramacion(cita: CitaResponse): void {
    this.citaReprogramar = cita;
    this.reprogramarForm.reset({ nuevaFechaHora: '', doctorId: '' });
  }

  reprogramarCita(): void {
    if (!this.citaReprogramar) {
      return;
    }
    const nuevaFechaHora = this.reprogramarForm.getRawValue().nuevaFechaHora;
    if (!nuevaFechaHora) {
      this.reprogramarForm.markAllAsTouched();
      return;
    }
    const doctorId = this.reprogramarForm.getRawValue().doctorId;
    this.procesando = true;
    this.citaService.reprogramar(this.citaReprogramar.id, nuevaFechaHora, doctorId ? Number(doctorId) : undefined)
      .pipe(finalize(() => (this.procesando = false)))
      .subscribe({
        next: () => {
          this.mensajeExito = 'Cita reprogramada correctamente.';
          this.citaReprogramar = null;
          this.cargarCitas();
        },
        error: () => {
          this.mensajeError = 'No se pudo reprogramar la cita.';
        }
      });
  }

  estadoClass(estado: string): string {
    if (estado === 'cancelada' || estado === 'no_asistida') {
      return 'is-danger';
    }
    if (estado === 'atendida' || estado === 'confirmada') {
      return 'is-success';
    }
    return 'is-muted';
  }

  fecha(cita: CitaResponse): string {
    return new Date(cita.fechaHoraInicio).toLocaleDateString();
  }

  hora(cita: CitaResponse): string {
    return new Date(cita.fechaHoraInicio).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  }

  private obtenerFiltros(): { pacienteId?: number; doctorId?: number; sedeId?: number; fecha?: string } {
    const raw = this.filtrosForm.getRawValue();
    return {
      pacienteId: raw.pacienteId ? Number(raw.pacienteId) : undefined,
      doctorId: raw.doctorId ? Number(raw.doctorId) : undefined,
      sedeId: this.sesionContextService.selectedSedeId || undefined,
      fecha: raw.fecha || undefined
    };
  }

  private aplicarPagina(response: Page<CitaResponse>): void {
    this.citas = response.content;
    this.page = response.number;
    this.size = response.size;
    this.totalPages = response.totalPages;
    this.totalElements = response.totalElements;
  }
}
