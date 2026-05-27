import { Component, OnInit } from '@angular/core';
import { FormBuilder } from '@angular/forms';
import { finalize, forkJoin } from 'rxjs';
import { AuthService } from '../../../core/services/auth.service';
import { EspecialidadResponse } from '../../../shared/models/especialidad.model';
import { MedicoResponse } from '../../../shared/models/medico.model';
import { Page } from '../../../shared/models/page.model';
import { SedeResponse } from '../../../shared/models/sede.model';
import { MedicoService } from '../../services/medico.service';

@Component({
  selector: 'app-listar-medicos',
  templateUrl: './listar-medicos.component.html',
  styleUrl: './listar-medicos.component.css'
})
export class ListarMedicosComponent implements OnInit {
  medicos: MedicoResponse[] = [];
  especialidades: EspecialidadResponse[] = [];
  sedes: SedeResponse[] = [];
  page = 0;
  size = 10;
  totalPages = 0;
  totalElements = 0;
  cargando = false;
  procesando = false;
  mensajeError = '';
  mensajeExito = '';
  medicoSeleccionado: MedicoResponse | null = null;

  filtrosForm = this.fb.group({
    texto: [''],
    especialidadId: [''],
    sedeId: ['']
  });

  constructor(
    public readonly authService: AuthService,
    private readonly fb: FormBuilder,
    private readonly medicoService: MedicoService
  ) {}

  ngOnInit(): void {
    forkJoin({
      especialidades: this.medicoService.getEspecialidades(),
      sedes: this.medicoService.getSedes()
    }).subscribe({
      next: ({ especialidades, sedes }) => {
        this.especialidades = especialidades.filter((item) => !item.especialidadPadreId);
        this.sedes = sedes;
      }
    });
    this.cargarMedicos();
  }

  cargarMedicos(page = this.page): void {
    this.cargando = true;
    this.mensajeError = '';
    this.medicoService.listar(page, this.size, this.obtenerFiltros())
      .pipe(finalize(() => (this.cargando = false)))
      .subscribe({
        next: (response) => this.aplicarPagina(response),
        error: () => {
          this.mensajeError = 'No se pudo cargar la lista de medicos.';
        }
      });
  }

  buscar(): void {
    this.cargarMedicos(0);
  }

  limpiarFiltros(): void {
    this.filtrosForm.reset({ texto: '', especialidadId: '', sedeId: '' });
    this.cargarMedicos(0);
  }

  abrirConfirmacion(medico: MedicoResponse): void {
    this.medicoSeleccionado = medico;
  }

  cerrarConfirmacion(): void {
    if (!this.procesando) {
      this.medicoSeleccionado = null;
    }
  }

  desactivarMedico(): void {
    if (!this.medicoSeleccionado) {
      return;
    }

    this.procesando = true;
    this.mensajeError = '';
    this.mensajeExito = '';
    this.medicoService.desactivar(this.medicoSeleccionado.id)
      .pipe(finalize(() => (this.procesando = false)))
      .subscribe({
        next: () => {
          this.mensajeExito = 'Medico desactivado correctamente.';
          this.medicoSeleccionado = null;
          this.cargarMedicos();
        },
        error: () => {
          this.mensajeError = 'No se pudo desactivar el medico.';
        }
      });
  }

  nombreCompleto(medico: MedicoResponse): string {
    return `${medico.nombres} ${medico.apellidos}`;
  }

  private obtenerFiltros(): { texto?: string; especialidadId?: number; sedeId?: number } {
    const raw = this.filtrosForm.getRawValue();
    return {
      texto: raw.texto || undefined,
      especialidadId: raw.especialidadId ? Number(raw.especialidadId) : undefined,
      sedeId: raw.sedeId ? Number(raw.sedeId) : undefined
    };
  }

  private aplicarPagina(response: Page<MedicoResponse>): void {
    this.medicos = response.content;
    this.page = response.number;
    this.size = response.size;
    this.totalPages = response.totalPages;
    this.totalElements = response.totalElements;
  }
}
