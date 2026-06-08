import { Component, OnInit } from '@angular/core';
import { finalize } from 'rxjs';
import { AuthService } from '../../../core/services/auth.service';
import { EspecialidadResponse } from '../../../shared/models/especialidad.model';
import { Page } from '../../../shared/models/page.model';
import { EspecialidadService } from '../../services/especialidad.service';

@Component({
    selector: 'app-listar-especialidades',
    templateUrl: './listar-especialidades.component.html',
    standalone: false
})
export class ListarEspecialidadesComponent implements OnInit {
  especialidades: EspecialidadResponse[] = [];
  page = 0;
  size = 10;
  totalPages = 0;
  totalElements = 0;
  cargando = false;
  mensajeError = '';
  mensajeExito = '';

  constructor(
    public readonly authService: AuthService,
    private readonly especialidadService: EspecialidadService
  ) {}

  ngOnInit(): void {
    this.cargarEspecialidades();
  }

  cargarEspecialidades(page = this.page): void {
    this.cargando = true;
    this.mensajeError = '';
    this.especialidadService.listar(page, this.size)
      .pipe(finalize(() => (this.cargando = false)))
      .subscribe({
        next: (response) => this.aplicarPagina(response),
        error: () => (this.mensajeError = 'No se pudo cargar la lista de especialidades.')
      });
  }

  eliminarEspecialidad(especialidad: EspecialidadResponse): void {
    this.especialidadService.eliminar(especialidad.id).subscribe({
      next: () => {
        this.mensajeExito = 'Especialidad eliminada correctamente.';
        this.cargarEspecialidades();
      },
      error: () => (this.mensajeError = 'No se pudo eliminar la especialidad.')
    });
  }

  private aplicarPagina(response: Page<EspecialidadResponse>): void {
    this.especialidades = response.content;
    this.page = response.number;
    this.size = response.size;
    this.totalPages = response.totalPages;
    this.totalElements = response.totalElements;
  }
}
