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
  today = new Date().toISOString().split('T')[0];
  slotsReprogramar: any[] = [];
  cargandoSlotsReprogramar = false;
  diasAtencionReprogramar = '';

  filtrosForm = this.fb.group({
    pacienteId: [''],
    doctorId: [''],
    fecha: ['']
  });

  reprogramarForm = this.fb.group({
    fecha: [''],
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
    if (!this.usaAgendaPropia) {
      forkJoin({
        pacientes: this.citaService.listarPacientes(),
        medicos: this.citaService.listarMedicos()
      }).subscribe({
        next: ({ pacientes, medicos }) => {
          this.pacientes = pacientes;
          this.medicos = medicos;
        }
      });
    }

    this.sesionContextService.selectedSedeId$.subscribe(() => {
      this.cargarCitas(0);
    });

    this.reprogramarForm.get('fecha')?.valueChanges.subscribe(() => {
      this.cargarSlotsReprogramar();
    });

    this.reprogramarForm.get('doctorId')?.valueChanges.subscribe((doctorId) => {
      const docId = doctorId ? Number(doctorId) : this.citaReprogramar?.doctorId;
      if (docId) {
        this.cargarDiasAtencionReprogramar(docId);
      }
      this.cargarSlotsReprogramar();
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
    this.slotsReprogramar = [];
    this.diasAtencionReprogramar = '';
    this.reprogramarForm.reset({ fecha: '', nuevaFechaHora: '', doctorId: '' });
    this.cargarDiasAtencionReprogramar(cita.doctorId);
  }

  medicosFiltrados(): MedicoResponse[] {
    if (!this.citaReprogramar) {
      return [];
    }
    const currentDoc = this.medicos.find(m => m.id === this.citaReprogramar!.doctorId);
    if (!currentDoc) {
      return this.medicos;
    }
    return this.medicos.filter(m => 
      m.especialidadNombre === currentDoc.especialidadNombre && 
      m.id !== currentDoc.id
    );
  }

  cargarSlotsReprogramar(): void {
    if (!this.citaReprogramar) {
      return;
    }
    const raw = this.reprogramarForm.getRawValue();
    const doctorId = raw.doctorId ? Number(raw.doctorId) : this.citaReprogramar.doctorId;
    const sedeId = this.citaReprogramar.sedeId;
    const fecha = raw.fecha;

    if (!fecha) {
      this.slotsReprogramar = [];
      return;
    }

    this.cargandoSlotsReprogramar = true;
    this.citaService.obtenerSlotsDisponibles(doctorId, sedeId, fecha)
      .pipe(finalize(() => (this.cargandoSlotsReprogramar = false)))
      .subscribe({
        next: (slots) => this.slotsReprogramar = slots,
        error: () => {
          this.slotsReprogramar = [];
        }
      });
  }

  cargarDiasAtencionReprogramar(doctorId: number): void {
    if (!doctorId) {
      this.diasAtencionReprogramar = '';
      return;
    }
    this.citaService.listarDisponibilidadBase(doctorId).subscribe({
      next: (bases) => {
        if (!bases || bases.length === 0) {
          this.diasAtencionReprogramar = 'Este médico no tiene días de atención configurados.';
          return;
        }
        const nombresDias = ['Lunes', 'Martes', 'Miércoles', 'Jueves', 'Viernes', 'Sábado', 'Domingo'];
        const dias = Array.from(new Set(bases.map(b => b.diaSemana)))
          .sort((a, b) => (a as number) - (b as number))
          .map(d => nombresDias[(d as number) - 1]);
        this.diasAtencionReprogramar = 'Días de atención: ' + dias.join(', ');
      },
      error: () => {
        this.diasAtencionReprogramar = '';
      }
    });
  }

  seleccionarSlotReprogramar(slot: any): void {
    this.reprogramarForm.patchValue({ nuevaFechaHora: slot.inicio });
  }

  horaSlot(slot: any): string {
    return new Date(slot.inicio).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
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
