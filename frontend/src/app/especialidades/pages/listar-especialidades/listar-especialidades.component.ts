import { Component, OnInit } from '@angular/core';
import { finalize } from 'rxjs';
import { AuthService } from '../../../core/services/auth.service';
import { EspecialidadResponse } from '../../../shared/models/especialidad.model';
import { Page } from '../../../shared/models/page.model';
import { EspecialidadService } from '../../services/especialidad.service';

@Component({
    selector: 'app-listar-especialidades',
    templateUrl: './listar-especialidades.component.html',
    styleUrl: './listar-especialidades.component.css',
    standalone: false
})
export class ListarEspecialidadesComponent implements OnInit {
  especialidades: EspecialidadResponse[] = [];
  todasEspecialidades: EspecialidadResponse[] = [];
  vistaJerarquica = true;
  soloPrincipales = false;

  busqueda = '';
  mostrarDropdown = false;
  expandedParents = new Set<number>();
  highlightedId: number | null = null;

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
    this.cargarTodas();
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

  cargarTodas(): void {
    this.especialidadService.listarTodas().subscribe({
      next: (res) => (this.todasEspecialidades = res),
      error: () => (this.mensajeError = 'No se pudo cargar la jerarquía de especialidades.')
    });
  }

  eliminarEspecialidad(especialidad: EspecialidadResponse): void {
    this.especialidadService.eliminar(especialidad.id).subscribe({
      next: () => {
        this.mensajeExito = 'Especialidad eliminada correctamente.';
        this.cargarEspecialidades();
        this.cargarTodas();
      },
      error: () => (this.mensajeError = 'No se pudo eliminar la especialidad.')
    });
  }

  get especialidadesJerarquicas() {
    const principales = this.todasEspecialidades.filter(e => !e.especialidadPadreId);
    return principales.map(p => ({
      ...p,
      subespecialidades: this.todasEspecialidades.filter(e => e.especialidadPadreId === p.id)
    }));
  }

  toggleExpand(parentId: number): void {
    if (this.expandedParents.has(parentId)) {
      this.expandedParents.delete(parentId);
    } else {
      this.expandedParents.add(parentId);
    }
  }

  isExpanded(parentId: number): boolean {
    return this.expandedParents.has(parentId);
  }

  get filteredOptions(): EspecialidadResponse[] {
    if (!this.busqueda) {
      return [];
    }
    const q = this.busqueda.toLowerCase();
    return this.todasEspecialidades.filter(e => e.nombre.toLowerCase().includes(q));
  }

  seleccionarCoincidencia(esp: EspecialidadResponse): void {
    this.busqueda = esp.nombre;
    this.mostrarDropdown = false;
    this.highlightedId = esp.id;

    if (esp.especialidadPadreId) {
      this.expandedParents.add(esp.especialidadPadreId);
    } else {
      this.expandedParents.add(esp.id);
    }

    setTimeout(() => {
      const el = document.getElementById('esp-row-' + esp.id);
      if (el) {
        el.scrollIntoView({ behavior: 'smooth', block: 'center' });
      }
    }, 150);

    setTimeout(() => {
      this.highlightedId = null;
    }, 4000);
  }

  limpiarBusqueda(): void {
    this.busqueda = '';
    this.highlightedId = null;
    this.mostrarDropdown = false;
  }

  ocultarDropdownConRetraso(): void {
    setTimeout(() => {
      this.mostrarDropdown = false;
    }, 200);
  }

  private aplicarPagina(response: Page<EspecialidadResponse>): void {
    this.especialidades = response.content;
    this.page = response.number;
    this.size = response.size;
    this.totalPages = response.totalPages;
    this.totalElements = response.totalElements;
  }
}
