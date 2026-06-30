import { Component, OnInit } from '@angular/core';
import { finalize } from 'rxjs';
import { UsuarioService } from '../../services/usuario.service';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-bitacora',
  templateUrl: './bitacora.component.html',
  styleUrl: './bitacora.component.css',
  standalone: false
})
export class BitacoraComponent implements OnInit {
  logs: any[] = [];
  filtroTexto = '';
  page = 0;
  size = 20;
  totalPages = 0;
  totalElements = 0;
  cargando = false;
  mensajeError = '';

  constructor(
    public readonly authService: AuthService,
    private readonly usuarioService: UsuarioService
  ) {}

  ngOnInit(): void {
    this.cargarLogs(0);
  }

  cargarLogs(page = this.page): void {
    this.cargando = true;
    this.mensajeError = '';
    this.usuarioService.listarAuditoria(page, this.size)
      .pipe(finalize(() => this.cargando = false))
      .subscribe({
        next: (res) => {
          this.logs = res.content.map((log: any) => ({
            ...log,
            verDetallesTecnicos: false
          }));
          this.page = res.number;
          this.totalPages = res.totalPages;
          this.totalElements = res.totalElements;
        },
        error: () => {
          this.mensajeError = 'No se pudo cargar el historial de auditoría.';
        }
      });
  }

  get logsFiltrados(): any[] {
    const q = this.filtroTexto.toLowerCase().trim();
    if (!q) return this.logs;
    return this.logs.filter(log => 
      (log.usuarioNombre && log.usuarioNombre.toLowerCase().includes(q)) ||
      (log.usuarioDni && log.usuarioDni.toLowerCase().includes(q)) ||
      (log.accion && log.accion.toLowerCase().includes(q)) ||
      (log.ipAddress && log.ipAddress.toLowerCase().includes(q)) ||
      (log.detalles && log.detalles.toLowerCase().includes(q)) ||
      (log.estado && log.estado.toLowerCase().includes(q))
    );
  }

  obtenerDetalleAmigable(detalles: string): string {
    if (!detalles) return 'Sin detalles adicionales';
    return detalles.split(' || ')[0];
  }

  obtenerDetalleTecnico(detalles: string): string {
    if (!detalles) return '';
    const parts = detalles.split(' || ');
    return parts.length > 1 ? parts[1] : '';
  }

  limpiarFiltro(): void {
    this.filtroTexto = '';
  }
}
