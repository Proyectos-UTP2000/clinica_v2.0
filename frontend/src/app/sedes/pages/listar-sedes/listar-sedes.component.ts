import { Component, OnInit } from '@angular/core';
import { finalize } from 'rxjs';
import { AuthService } from '../../../core/services/auth.service';
import { Page } from '../../../shared/models/page.model';
import { SedeResponse } from '../../../shared/models/sede.model';
import { SedeService } from '../../services/sede.service';

@Component({
    selector: 'app-listar-sedes',
    templateUrl: './listar-sedes.component.html',
    standalone: false
})
export class ListarSedesComponent implements OnInit {
  sedes: SedeResponse[] = [];
  page = 0;
  size = 10;
  totalPages = 0;
  totalElements = 0;
  cargando = false;
  mensajeError = '';
  mensajeExito = '';

  constructor(
    public readonly authService: AuthService,
    private readonly sedeService: SedeService
  ) {}

  ngOnInit(): void {
    this.cargarSedes();
  }

  cargarSedes(page = this.page): void {
    this.cargando = true;
    this.mensajeError = '';
    this.sedeService.listar(page, this.size)
      .pipe(finalize(() => (this.cargando = false)))
      .subscribe({
        next: (response) => this.aplicarPagina(response),
        error: () => (this.mensajeError = 'No se pudo cargar la lista de sedes.')
      });
  }

  desactivarSede(sede: SedeResponse): void {
    this.sedeService.desactivar(sede.id).subscribe({
      next: () => {
        this.mensajeExito = 'Sede desactivada correctamente.';
        this.cargarSedes();
      },
      error: () => (this.mensajeError = 'No se pudo desactivar la sede.')
    });
  }

  private aplicarPagina(response: Page<SedeResponse>): void {
    this.sedes = response.content;
    this.page = response.number;
    this.size = response.size;
    this.totalPages = response.totalPages;
    this.totalElements = response.totalElements;
  }
}
