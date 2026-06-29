import { Component, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { finalize, forkJoin } from 'rxjs';
import { AuthService } from '../../../core/services/auth.service';
import { SesionContextService } from '../../../core/services/sesion-context.service';
import { ConsultaResponse } from '../../../shared/models/consulta.model';
import { MedicoResponse } from '../../../shared/models/medico.model';
import { PacienteResponse } from '../../../shared/models/paciente.model';
import { Page } from '../../../shared/models/page.model';
import { SedeResponse } from '../../../shared/models/sede.model';
import { HistorialService } from '../../services/historial.service';

@Component({
    selector: 'app-listar-historial',
    templateUrl: './listar-historial.component.html',
    standalone: false
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
  dniBusqueda = '';

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
    private readonly sesionContextService: SesionContextService,
    private readonly historialService: HistorialService,
    private readonly fb: FormBuilder
  ) {}

  ngOnInit(): void {
    this.cargarDatosIniciales();
  }

  cargarDatosIniciales(): void {
    this.cargando = true;
    this.mensajeError = '';

    const tienePacientesVer = this.authService.hasPermission('pacientes.ver');
    const tieneMedicosVer = this.authService.hasPermission('medicos.ver');

    this.sesionContextService.sedes$.subscribe((sedes) => {
      this.sedes = sedes;
    });

    const calls: any = {};
    if (tienePacientesVer) {
      calls.pacientes = this.historialService.listarPacientes();
    }
    if (tieneMedicosVer) {
      calls.medicos = this.historialService.listarMedicos();
    } else if (this.authService.hasRole('Doctor')) {
      calls.medicoAutenticado = this.historialService.obtenerMedicoAutenticado();
      calls.citasDoctor = this.historialService.listarCitasDoctor();
    }

    if (Object.keys(calls).length === 0) {
      this.cargando = false;
      this.consultaForm.patchValue({
        sedeId: this.sedes[0]?.id ?? null
      });
      return;
    }

    forkJoin(calls)
      .pipe(finalize(() => (this.cargando = false)))
      .subscribe({
        next: (res: any) => {
          if (res.pacientes) {
            this.pacientes = res.pacientes;
          }
          if (res.medicos) {
            this.medicos = res.medicos;
          }
          if (res.citasDoctor && res.citasDoctor.content) {
            const list = res.citasDoctor.content.map((c: any) => ({
              id: c.pacienteId,
              nombres: c.pacienteNombre,
              apellidos: '',
              dni: '',
              email: '',
              telefono: '',
              activo: true
            }));
            list.forEach((p: any) => {
              if (!this.pacientes.some((existing) => existing.id === p.id)) {
                this.pacientes.push(p);
              }
            });
          }

          const rutaPacienteId = this.route.snapshot.paramMap.get('pacienteId');
          let pacienteId: number | null = null;
          if (rutaPacienteId) {
            pacienteId = Number(rutaPacienteId);
          } else if (this.pacientes.length > 0) {
            pacienteId = this.pacientes[0].id;
          }

          const completeLoading = () => {
            this.filtroForm.patchValue({ pacienteId });

            let doctorId: number | null = null;
            if (res.medicoAutenticado) {
              doctorId = res.medicoAutenticado.id;
            } else if (this.medicos.length > 0) {
              doctorId = this.medicos[0].id;
            }

            this.consultaForm.patchValue({
              pacienteId,
              doctorId,
              sedeId: this.sedes[0]?.id ?? null
            });

            if (pacienteId) {
              this.cargarHistorial(0);
            }
          };

          if (pacienteId && !this.pacientes.some(p => p.id === pacienteId)) {
            this.cargando = true;
            this.historialService.obtenerPacientePorId(pacienteId)
              .pipe(finalize(() => (this.cargando = false)))
              .subscribe({
                next: (paciente) => {
                  this.pacientes.push(paciente);
                  completeLoading();
                },
                error: () => {
                  completeLoading();
                }
              });
          } else {
            completeLoading();
          }
        },
        error: () => (this.mensajeError = 'No se pudo cargar la informacion de historial.')
      });
  }

  buscarPaciente(): void {
    if (!this.dniBusqueda) {
      return;
    }
    this.cargando = true;
    this.mensajeError = '';
    this.mensajeExito = '';
    this.historialService.buscarPacientePorDni(this.dniBusqueda)
      .pipe(finalize(() => (this.cargando = false)))
      .subscribe({
        next: (paciente) => {
          this.pacientes = [paciente];
          this.filtroForm.patchValue({ pacienteId: paciente.id });
          this.consultaForm.patchValue({ pacienteId: paciente.id });
          this.cargarHistorial(0);
        },
        error: () => {
          this.mensajeError = 'No se encontró ningún paciente con el DNI ingresado o no está autorizado.';
        }
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
