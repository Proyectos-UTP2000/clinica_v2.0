import { Component, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { Router } from '@angular/router';
import { finalize, forkJoin } from 'rxjs';
import { CitaCreateRequest, DisponibilidadSlotResponse } from '../../../shared/models/cita.model';
import { MedicoResponse } from '../../../shared/models/medico.model';
import { PacienteResponse } from '../../../shared/models/paciente.model';
import { SedeResponse } from '../../../shared/models/sede.model';
import { ConsultorioResponse } from '../../../shared/models/consultorio.model';
import { CitaService } from '../../services/cita.service';
import { ConsultorioService } from '../../../consultorios/services/consultorio.service';
import { ApiErrorService } from '../../../core/services/api-error.service';

@Component({
    selector: 'app-crear-cita',
    templateUrl: './crear-cita.component.html',
    styleUrl: './crear-cita.component.css',
    standalone: false
})
export class CrearCitaComponent implements OnInit {
  pacientes: PacienteResponse[] = [];
  medicos: MedicoResponse[] = [];
  sedes: SedeResponse[] = [];
  consultorios: ConsultorioResponse[] = [];
  slots: DisponibilidadSlotResponse[] = [];
  cargandoCatalogos = false;
  cargandoSlots = false;
  cargandoConsultorios = false;
  guardando = false;
  mensajeError = '';
  today = new Date().toISOString().split('T')[0];

  citaForm = this.fb.group({
    pacienteId: ['', [Validators.required]],
    doctorId: ['', [Validators.required]],
    sedeId: ['', [Validators.required]],
    consultorioId: ['', [Validators.required]],
    fecha: ['', [Validators.required]],
    fechaHoraInicio: ['', [Validators.required]],
    pagoAnticipado: [false]
  });

  constructor(
    private readonly fb: FormBuilder,
    private readonly citaService: CitaService,
    private readonly consultorioService: ConsultorioService,
    private readonly apiErrorService: ApiErrorService,
    private readonly route: ActivatedRoute,
    private readonly router: Router
  ) {}

  ngOnInit(): void {
    this.cargarCatalogos();
    this.citaForm.valueChanges.subscribe(() => {
      const raw = this.citaForm.getRawValue();
      if (raw.doctorId && raw.sedeId && raw.fecha) {
        this.cargarSlots();
      } else {
        this.slots = [];
      }
    });

    this.citaForm.get('sedeId')?.valueChanges.subscribe((sedeId) => {
      if (sedeId) {
        this.cargarConsultorios(Number(sedeId));
      } else {
        this.consultorios = [];
        this.citaForm.patchValue({ consultorioId: '' });
      }
    });

    this.citaForm.get('doctorId')?.valueChanges.subscribe(() => {
      this.sugerirConsultorio();
    });
  }

  cargarConsultorios(sedeId: number): void {
    this.cargandoConsultorios = true;
    this.consultorioService.listarPorSede(sedeId)
      .pipe(finalize(() => (this.cargandoConsultorios = false)))
      .subscribe({
        next: (res) => {
          this.consultorios = res;
          this.sugerirConsultorio();
        },
        error: (err) => (this.mensajeError = this.apiErrorService.obtenerMensajeError(err))
      });
  }

  sugerirConsultorio(): void {
    const doctorId = Number(this.citaForm.value.doctorId);
    const doctor = this.medicos.find(d => d.id === doctorId);
    if (doctor && doctor.consultorioIds && doctor.consultorioIds.length > 0) {
      const sugerido = this.consultorios.find(c => doctor.consultorioIds!.includes(c.id));
      if (sugerido) {
        this.citaForm.patchValue({ consultorioId: String(sugerido.id) });
      }
    }
  }

  cargarSlots(): void {
    const raw = this.citaForm.getRawValue();
    if (!raw.doctorId || !raw.sedeId || !raw.fecha || this.cargandoSlots) {
      return;
    }

    this.cargandoSlots = true;
    this.slots = [];
    this.citaService.obtenerSlotsDisponibles(Number(raw.doctorId), Number(raw.sedeId), raw.fecha)
      .pipe(finalize(() => (this.cargandoSlots = false)))
      .subscribe({
        next: (slots) => this.slots = slots,
        error: () => {
          this.mensajeError = 'No se pudieron cargar los horarios disponibles.';
        }
      });
  }

  seleccionarSlot(slot: DisponibilidadSlotResponse): void {
    this.citaForm.patchValue({ fechaHoraInicio: slot.inicio }, { emitEvent: false });
  }

  guardar(): void {
    this.mensajeError = '';
    this.citaForm.markAllAsTouched();
    if (this.citaForm.invalid) {
      return;
    }

    this.guardando = true;
    this.citaService.crear(this.construirRequest())
      .pipe(finalize(() => (this.guardando = false)))
      .subscribe({
        next: () => this.router.navigateByUrl('/citas'),
        error: (err) => {
          this.mensajeError = this.apiErrorService.obtenerMensajeError(err);
        }
      });
  }

  campoInvalido(campo: string): boolean {
    const control = this.citaForm.get(campo);
    return Boolean(control?.invalid && (control.dirty || control.touched));
  }

  slotSeleccionado(slot: DisponibilidadSlotResponse): boolean {
    return this.citaForm.getRawValue().fechaHoraInicio === slot.inicio;
  }

  horaSlot(slot: DisponibilidadSlotResponse): string {
    return new Date(slot.inicio).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  }

  get pacienteSeleccionado(): PacienteResponse | undefined {
    const id = Number(this.citaForm.getRawValue().pacienteId);
    return this.pacientes.find((paciente) => paciente.id === id);
  }

  get medicoSeleccionado(): MedicoResponse | undefined {
    const id = Number(this.citaForm.getRawValue().doctorId);
    return this.medicos.find((medico) => medico.id === id);
  }

  get sedeSeleccionada(): SedeResponse | undefined {
    const id = Number(this.citaForm.getRawValue().sedeId);
    return this.sedes.find((sede) => sede.id === id);
  }

  get consultorioSeleccionado(): ConsultorioResponse | undefined {
    const id = Number(this.citaForm.getRawValue().consultorioId);
    return this.consultorios.find((c) => c.id === id);
  }

  private cargarCatalogos(): void {
    this.cargandoCatalogos = true;
    forkJoin({
      pacientes: this.citaService.listarPacientes(),
      medicos: this.citaService.listarMedicos(),
      sedes: this.citaService.listarSedes()
    })
      .pipe(finalize(() => (this.cargandoCatalogos = false)))
      .subscribe({
        next: ({ pacientes, medicos, sedes }) => {
          this.pacientes = pacientes;
          this.medicos = medicos;
          this.sedes = sedes;
          this.aplicarPrefillAgenda();
        },
        error: () => {
          this.mensajeError = 'No se pudieron cargar pacientes, medicos o sedes.';
        }
      });
  }

  private construirRequest(): CitaCreateRequest {
    const raw = this.citaForm.getRawValue();
    return {
      pacienteId: Number(raw.pacienteId),
      doctorId: Number(raw.doctorId),
      sedeId: Number(raw.sedeId),
      consultorioId: Number(raw.consultorioId),
      fechaHoraInicio: raw.fechaHoraInicio ?? '',
      pagoAnticipado: Boolean(raw.pagoAnticipado)
    };
  }

  private aplicarPrefillAgenda(): void {
    const query = this.route.snapshot.queryParamMap;
    const fechaHoraInicio = query.get('fechaHoraInicio');
    this.citaForm.patchValue({
      doctorId: query.get('doctorId') || '',
      sedeId: query.get('sedeId') || '',
      fecha: fechaHoraInicio ? fechaHoraInicio.slice(0, 10) : ''
    });
    if (fechaHoraInicio) {
      this.citaForm.patchValue({ fechaHoraInicio }, { emitEvent: false });
    }
  }
}
