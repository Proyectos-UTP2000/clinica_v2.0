import { Component, OnInit } from '@angular/core';
import { finalize } from 'rxjs';
import { AuthService } from '../../../core/services/auth.service';
import { PacienteResponse } from '../../../shared/models/paciente.model';
import { Page } from '../../../shared/models/page.model';
import { PacienteService } from '../../services/paciente.service';

@Component({
  selector: 'app-listar-pacientes',
  templateUrl: './listar-pacientes.component.html',
  styleUrl: './listar-pacientes.component.css'
})
export class ListarPacientesComponent implements OnInit {
  pacientes: PacienteResponse[] = [];
  page = 0;
  size = 10;
  totalPages = 0;
  totalElements = 0;
  cargando = false;
  procesando = false;
  mensajeError = '';
  mensajeExito = '';
  pacienteSeleccionado: PacienteResponse | null = null;

  constructor(
    public readonly authService: AuthService,
    private readonly pacienteService: PacienteService
  ) {}

  ngOnInit(): void {
    this.cargarPacientes();
  }

  cargarPacientes(page = this.page): void {
    this.cargando = true;
    this.mensajeError = '';
    this.pacienteService.listar(page, this.size)
      .pipe(finalize(() => (this.cargando = false)))
      .subscribe({
        next: (response) => this.aplicarPagina(response),
        error: () => {
          this.mensajeError = 'No se pudo cargar la lista de pacientes.';
        }
      });
  }

  abrirConfirmacion(paciente: PacienteResponse): void {
    this.pacienteSeleccionado = paciente;
  }

  cerrarConfirmacion(): void {
    if (!this.procesando) {
      this.pacienteSeleccionado = null;
    }
  }

  desactivarPaciente(): void {
    if (!this.pacienteSeleccionado) {
      return;
    }

    this.procesando = true;
    this.mensajeError = '';
    this.mensajeExito = '';
    this.pacienteService.desactivar(this.pacienteSeleccionado.id)
      .pipe(finalize(() => (this.procesando = false)))
      .subscribe({
        next: () => {
          this.mensajeExito = 'Paciente desactivado correctamente.';
          this.pacienteSeleccionado = null;
          this.cargarPacientes();
        },
        error: () => {
          this.mensajeError = 'No se pudo desactivar el paciente.';
        }
      });
  }

  nombreCompleto(paciente: PacienteResponse): string {
    return `${paciente.nombres} ${paciente.apellidos}`;
  }

  private aplicarPagina(response: Page<PacienteResponse>): void {
    this.pacientes = response.content;
    this.page = response.number;
    this.size = response.size;
    this.totalPages = response.totalPages;
    this.totalElements = response.totalElements;
  }
}
