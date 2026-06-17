import { Component, OnInit } from '@angular/core';
import { finalize } from 'rxjs';
import { AuthService } from '../../../core/services/auth.service';
import { Page } from '../../../shared/models/page.model';
import { RolResponse } from '../../../shared/models/rol.model';
import { RolService } from '../../services/rol.service';

@Component({
    selector: 'app-listar-roles',
    templateUrl: './listar-roles.component.html',
    standalone: false
})
export class ListarRolesComponent implements OnInit {
  roles: RolResponse[] = [];
  page = 0;
  size = 10;
  totalPages = 0;
  totalElements = 0;
  cargando = false;
  mensajeError = '';
  mensajeExito = '';

  constructor(
    public readonly authService: AuthService,
    private readonly rolService: RolService
  ) {}

  ngOnInit(): void {
    this.cargarRoles();
  }

  cargarRoles(page = this.page): void {
    this.cargando = true;
    this.mensajeError = '';
    this.rolService.listar(page, this.size)
      .pipe(finalize(() => (this.cargando = false)))
      .subscribe({
        next: (response) => this.aplicarPagina(response),
        error: () => (this.mensajeError = 'No se pudo cargar la lista de roles.')
      });
  }

  desactivarRol(rol: RolResponse): void {
    this.rolService.desactivar(rol.id).subscribe({
      next: () => {
        this.mensajeExito = 'Rol desactivado correctamente.';
        this.cargarRoles();
      },
      error: () => (this.mensajeError = 'No se pudo desactivar el rol.')
    });
  }

  permisosResumen(rol: RolResponse): string {
    const total = rol.permisos?.length ?? 0;
    if (total === 0) {
      return 'Sin permisos asignados';
    }
    return `${total} permiso${total === 1 ? '' : 's'} asignado${total === 1 ? '' : 's'}`;
  }

  private aplicarPagina(response: Page<RolResponse>): void {
    this.roles = response.content;
    this.page = response.number;
    this.size = response.size;
    this.totalPages = response.totalPages;
    this.totalElements = response.totalElements;
  }
}
