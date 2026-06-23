import { Component, OnInit } from '@angular/core';
import { finalize } from 'rxjs';
import { AuthService } from '../../../core/services/auth.service';
import { Page } from '../../../shared/models/page.model';
import { ConsultorioResponse } from '../../../shared/models/consultorio.model';
import { ConsultorioService } from '../../services/consultorio.service';
import { ApiErrorService } from '../../../core/services/api-error.service';

@Component({
  selector: 'app-listar-consultorios',
  templateUrl: './listar-consultorios.component.html',
  standalone: false
})
export class ListarConsultoriosComponent implements OnInit {
  consultorios: ConsultorioResponse[] = [];
  page = 0;
  size = 10;
  totalPages = 0;
  totalElements = 0;
  cargando = false;
  mensajeError = '';
  mensajeExito = '';

  constructor(
    public readonly authService: AuthService,
    private readonly consultorioService: ConsultorioService,
    private readonly apiErrorService: ApiErrorService
  ) {}

  ngOnInit(): void {
    this.cargarConsultorios();
  }

  cargarConsultorios(page = this.page): void {
    this.cargando = true;
    this.mensajeError = '';
    this.consultorioService.listar(page, this.size)
      .pipe(finalize(() => (this.cargando = false)))
      .subscribe({
        next: (response) => this.aplicarPagina(response),
        error: (err) => (this.mensajeError = this.apiErrorService.obtenerMensajeError(err))
      });
  }

  desactivarConsultorio(consultorio: ConsultorioResponse): void {
    this.consultorioService.desactivar(consultorio.id).subscribe({
      next: () => {
        this.mensajeExito = 'Consultorio desactivado correctamente.';
        this.cargarConsultorios();
      },
      error: (err) => (this.mensajeError = this.apiErrorService.obtenerMensajeError(err))
    });
  }

  private aplicarPagina(response: Page<ConsultorioResponse>): void {
    this.consultorios = response.content;
    this.page = response.number;
    this.size = response.size;
    this.totalPages = response.totalPages;
    this.totalElements = response.totalElements;
  }
}
