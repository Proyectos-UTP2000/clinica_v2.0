import { Component, OnInit } from '@angular/core';
import { finalize } from 'rxjs';
import { AuthService } from '../../../core/services/auth.service';
import { Page } from '../../../shared/models/page.model';
import { UsuarioResponse } from '../../../shared/models/usuario.model';
import { UsuarioService } from '../../services/usuario.service';

@Component({
  selector: 'app-listar-usuarios',
  templateUrl: './listar-usuarios.component.html',
  standalone: false
})
export class ListarUsuariosComponent implements OnInit {
  usuarios: UsuarioResponse[] = [];
  page = 0;
  size = 10;
  totalPages = 0;
  totalElements = 0;
  cargando = false;
  mensajeError = '';
  mensajeExito = '';

  constructor(
    public readonly authService: AuthService,
    private readonly usuarioService: UsuarioService
  ) {}

  ngOnInit(): void {
    this.cargarUsuarios();
  }

  cargarUsuarios(page = this.page): void {
    this.cargando = true;
    this.mensajeError = '';
    this.usuarioService.listar(page, this.size)
      .pipe(finalize(() => (this.cargando = false)))
      .subscribe({
        next: (response) => this.aplicarPagina(response),
        error: () => (this.mensajeError = 'No se pudo cargar la lista de empleados.')
      });
  }

  desactivarUsuario(usuario: UsuarioResponse): void {
    this.usuarioService.desactivar(usuario.id).subscribe({
      next: () => {
        this.mensajeExito = `El estado del empleado ${usuario.nombres} ha sido modificado.`;
        this.cargarUsuarios();
      },
      error: () => (this.mensajeError = 'No se pudo cambiar el estado del empleado.')
    });
  }

  rolesResumen(usuario: UsuarioResponse): string {
    const roles = usuario.roles ?? [];
    if (roles.length === 0) {
      return 'Sin roles asignados';
    }
    return roles.map((r) => r.nombre).join(', ');
  }

  private aplicarPagina(response: Page<UsuarioResponse>): void {
    this.usuarios = response.content;
    this.page = response.number;
    this.size = response.size;
    this.totalPages = response.totalPages;
    this.totalElements = response.totalElements;
  }
}
