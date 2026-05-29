import { Component, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { finalize, forkJoin } from 'rxjs';
import { AuthService } from '../../../core/services/auth.service';
import { ConsultaResponse } from '../../../shared/models/consulta.model';
import { MedicoResponse } from '../../../shared/models/medico.model';
import { PacienteResponse } from '../../../shared/models/paciente.model';
import { Page } from '../../../shared/models/page.model';
import { SedeResponse } from '../../../shared/models/sede.model';
import { HistorialService } from '../../services/historial.service';

@Component({
  selector: 'app-listar-historial',
  templateUrl: './listar-historial.component.html'
})
export class ListarHistorialComponent implements OnInit {
  pacientes: PacienteResponse[] = [];
  medicos: MedicoResponse[] = [];
  sedes: SedeResponse[] = [];
  consultas: ConsultaResponse[] = [];
  page = 0;
  size = 10;
  totalPages = 0;
  totalElements = 0;
  cargando = false;
  guardando = false;
  mensajeError = '';
  mensajeExito = '';

  filtroForm = this.fb.group({
    pacienteId: [null as number | null, Validators.required]
  });

  consultaForm = this.fb.group({
    pacienteId: [null as number | null, Validators.required],
    doctorId: [null as number | null, Validators.required],
    sedeId: [null as number | null, Validators.required],
    tipo: ['consulta', Validators.required],
    motivoConsulta: ['Control general'],
    diagnostico: ['Paciente estable'],
    observaciones: ['Sin observaciones relevantes']
  });

  constructor(
    private readonly route: ActivatedRoute,
    public readonly authService: AuthService,
    private readonly historialService: HistorialService,
    private readonly fb: FormBuilder
  ) {}

  ngOnInit(): void {
    this.cargarDatosIniciales();
  }

  cargarDatosIniciales(): void {
    this.cargando = true;
    this.mensajeError = '';
    forkJoin({
      pacientes: this.historialService.listarPacientes(),
      medicos: this.historialService.listarMedicos(),
      sedes: this.historialService.listarSedes()
    })
      .pipe(finalize(() => (this.cargando = false)))
      .subscribe({
        next: ({ pacientes, medicos, sedes }) => {
          this.pacientes = pacientes;
          this.medicos = medicos;
          this.sedes = sedes;
          const rutaPacienteId = this.route.snapshot.paramMap.get('pacienteId');
          const pacienteId = rutaPacienteId ? Number(rutaPacienteId) : pacientes[0]?.id ?? null;
          this.filtroForm.patchValue({ pacienteId });
          this.consultaForm.patchValue({
            pacienteId,
            doctorId: medicos[0]?.id ?? null,
            sedeId: sedes[0]?.id ?? null
          });
          if (pacienteId) {
            this.cargarHistorial(0);
          }
        },
        error: () => (this.mensajeError = 'No se pudo cargar la informacion de historial.')
      });
  }

  cargarHistorial(page = this.page): void {
    const pacienteId = this.filtroForm.value.pacienteId;
    if (!pacienteId) {
      this.consultas = [];
      return;
    }
    this.cargando = true;
    this.historialService.listarPorPaciente(pacienteId, page, this.size)
      .pipe(finalize(() => (this.cargando = false)))
      .subscribe({
        next: (response) => this.aplicarPagina(response),
        error: () => (this.mensajeError = 'No se pudo cargar el historial del paciente.')
      });
  }

  crearConsulta(): void {
    if (this.consultaForm.invalid) {
      this.consultaForm.markAllAsTouched();
      return;
    }
    const value = this.consultaForm.getRawValue();
    this.guardando = true;
    this.mensajeError = '';
    this.mensajeExito = '';
    this.historialService.crear({
      pacienteId: Number(value.pacienteId),
      doctorId: Number(value.doctorId),
      sedeId: Number(value.sedeId),
      tipo: value.tipo || 'consulta',
      motivoConsulta: value.motivoConsulta || undefined,
      diagnostico: value.diagnostico || undefined,
      observaciones: value.observaciones || undefined
    })
      .pipe(finalize(() => (this.guardando = false)))
      .subscribe({
        next: () => {
          this.mensajeExito = 'Consulta registrada correctamente.';
          this.filtroForm.patchValue({ pacienteId: Number(value.pacienteId) });
          this.cargarHistorial(0);
        },
        error: () => (this.mensajeError = 'No se pudo registrar la consulta.')
      });
  }

  private aplicarPagina(response: Page<ConsultaResponse>): void {
    this.consultas = response.content;
    this.page = response.number;
    this.size = response.size;
    this.totalPages = response.totalPages;
    this.totalElements = response.totalElements;
  }
}
