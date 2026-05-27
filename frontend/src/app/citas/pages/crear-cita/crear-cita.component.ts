import { Component, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { finalize, forkJoin } from 'rxjs';
import { CitaCreateRequest, DisponibilidadSlotResponse } from '../../../shared/models/cita.model';
import { MedicoResponse } from '../../../shared/models/medico.model';
import { PacienteResponse } from '../../../shared/models/paciente.model';
import { SedeResponse } from '../../../shared/models/sede.model';
import { CitaService } from '../../services/cita.service';

@Component({
  selector: 'app-crear-cita',
  templateUrl: './crear-cita.component.html',
  styleUrl: './crear-cita.component.css'
})
export class CrearCitaComponent implements OnInit {
  pacientes: PacienteResponse[] = [];
  medicos: MedicoResponse[] = [];
  sedes: SedeResponse[] = [];
  slots: DisponibilidadSlotResponse[] = [];
  cargandoCatalogos = false;
  cargandoSlots = false;
  guardando = false;
  mensajeError = '';

  citaForm = this.fb.group({
    pacienteId: ['', [Validators.required]],
    doctorId: ['', [Validators.required]],
    sedeId: ['', [Validators.required]],
    fecha: ['', [Validators.required]],
    fechaHoraInicio: ['', [Validators.required]]
  });

  constructor(
    private readonly fb: FormBuilder,
    private readonly citaService: CitaService,
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
        error: () => {
          this.mensajeError = 'No se pudo agendar la cita. Verifique disponibilidad y datos seleccionados.';
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
      fechaHoraInicio: raw.fechaHoraInicio ?? ''
    };
  }
}
