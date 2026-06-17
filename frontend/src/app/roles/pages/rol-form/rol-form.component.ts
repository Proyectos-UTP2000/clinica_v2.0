import { Component, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { catchError, finalize, of, switchMap } from 'rxjs';
import { AuthService } from '../../../core/services/auth.service';
import { PermisoResponse } from '../../../shared/models/rol.model';
import { RolService } from '../../services/rol.service';

interface GrupoPermisos {
  codigo: string;
  titulo: string;
  descripcion: string;
  permisos: PermisoResponse[];
}

const GRUPOS: Record<string, { titulo: string; descripcion: string }> = {
  dashboard: { titulo: 'Dashboard', descripcion: 'Acceso al panel general del sistema.' },
  roles: { titulo: 'Roles y permisos', descripcion: 'Administracion de perfiles y permisos.' },
  usuarios: { titulo: 'Usuarios / empleados', descripcion: 'Gestion de cuentas internas no medicas.' },
  pacientes: { titulo: 'Pacientes', descripcion: 'CRUD y consulta de pacientes.' },
  medicos: { titulo: 'Medicos', descripcion: 'Gestion de medicos y datos asociados.' },
  sedes: { titulo: 'Sedes', descripcion: 'Administracion de sedes de atencion.' },
  especialidades: { titulo: 'Especialidades', descripcion: 'Catalogo de especialidades y subespecialidades.' },
  consultorios: { titulo: 'Consultorios', descripcion: 'Gestion futura de consultorios por sede.' },
  citas: { titulo: 'Citas y agenda', descripcion: 'Agendamiento, visualizacion y reprogramacion.' },
  pagos: { titulo: 'Pagos', descripcion: 'Consulta y registro manual de pagos.' },
  historial: { titulo: 'Historial clinico', descripcion: 'Consultas, evolucion y lectura clinica.' },
  disponibilidad: { titulo: 'Disponibilidad', descripcion: 'Horarios base y excepciones medicas.' },
  justificaciones: { titulo: 'Justificaciones', descripcion: 'Gestion de justificaciones medicas.' },
  reportes: { titulo: 'Reportes', descripcion: 'Analiticas y reportes administrativos.' }
};

@Component({
    selector: 'app-rol-form',
    templateUrl: './rol-form.component.html',
    standalone: false
})
export class RolFormComponent implements OnInit {
  rolId: number | null = null;
  permisos: PermisoResponse[] = [];
  permisosSeleccionados = new Set<number>();
  cargando = false;
  guardando = false;
  mensajeError = '';
  filtroPermisos = '';

  rolForm = this.fb.group({
    nombre: ['', Validators.required],
    descripcion: [''],
    activo: [true]
  });

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly fb: FormBuilder,
    private readonly rolService: RolService,
    private readonly authService: AuthService
  ) {}

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    this.rolId = idParam ? Number(idParam) : null;
    this.cargarPermisos();
    if (this.rolId) {
      this.cargarRol(this.rolId);
    }
  }

  get totalSeleccionados(): number {
    return this.permisosSeleccionados.size;
  }

  get gruposPermisos(): GrupoPermisos[] {
    const filtro = this.filtroPermisos.trim().toLowerCase();
    const permisosFiltrados = this.permisos.filter((permiso) => {
      if (!filtro) {
        return true;
      }
      return permiso.codigo.toLowerCase().includes(filtro)
        || (permiso.descripcion ?? '').toLowerCase().includes(filtro);
    });
    const porGrupo = new Map<string, PermisoResponse[]>();
    permisosFiltrados.forEach((permiso) => {
      const grupo = permiso.codigo.split('.')[0];
      porGrupo.set(grupo, [...(porGrupo.get(grupo) ?? []), permiso]);
    });
    return Array.from(porGrupo.entries())
      .map(([codigo, permisos]) => ({
        codigo,
        titulo: GRUPOS[codigo]?.titulo ?? this.capitalizar(codigo),
        descripcion: GRUPOS[codigo]?.descripcion ?? 'Permisos asociados a esta gestion.',
        permisos: permisos.sort((a, b) => a.codigo.localeCompare(b.codigo))
      }))
      .sort((a, b) => a.titulo.localeCompare(b.titulo));
  }

  togglePermiso(permisoId: number, seleccionado: boolean): void {
    if (seleccionado) {
      this.permisosSeleccionados.add(permisoId);
      return;
    }
    this.permisosSeleccionados.delete(permisoId);
  }

  permisoSeleccionado(permisoId: number): boolean {
    return this.permisosSeleccionados.has(permisoId);
  }

  seleccionadosEnGrupo(grupo: GrupoPermisos): number {
    return grupo.permisos.filter((permiso) => this.permisosSeleccionados.has(permiso.id)).length;
  }

  grupoCompleto(grupo: GrupoPermisos): boolean {
    return grupo.permisos.length > 0 && this.seleccionadosEnGrupo(grupo) === grupo.permisos.length;
  }

  seleccionarGrupo(grupo: GrupoPermisos): void {
    grupo.permisos.forEach((permiso) => this.permisosSeleccionados.add(permiso.id));
  }

  limpiarGrupo(grupo: GrupoPermisos): void {
    grupo.permisos.forEach((permiso) => this.permisosSeleccionados.delete(permiso.id));
  }

  guardar(): void {
    if (this.rolForm.invalid) {
      this.rolForm.markAllAsTouched();
      return;
    }
    const request = {
      nombre: this.rolForm.value.nombre || '',
      descripcion: this.rolForm.value.descripcion || undefined,
      permisosIds: Array.from(this.permisosSeleccionados).sort((a, b) => a - b)
    };
    this.guardando = true;
    this.mensajeError = '';
    const operacion = this.rolId
      ? this.rolService.actualizar(this.rolId, { ...request, activo: this.rolForm.value.activo ?? true })
      : this.rolService.crear(request);
    operacion.pipe(
      switchMap(() => this.authService.refrescarSesion().pipe(catchError(() => of(null)))),
      finalize(() => (this.guardando = false))
    ).subscribe({
      next: () => this.router.navigate(['/roles']),
      error: () => (this.mensajeError = 'No se pudo guardar el rol.')
    });
  }

  private cargarPermisos(): void {
    this.rolService.listarPermisos().subscribe({
      next: (permisos) => (this.permisos = permisos),
      error: () => (this.mensajeError = 'No se pudo cargar la lista de permisos.')
    });
  }

  private cargarRol(id: number): void {
    this.cargando = true;
    this.rolService.obtener(id)
      .pipe(finalize(() => (this.cargando = false)))
      .subscribe({
        next: (rol) => {
          this.rolForm.patchValue({
            nombre: rol.nombre,
            descripcion: rol.descripcion,
            activo: rol.activo
          });
          this.permisosSeleccionados = new Set(rol.permisos.map((permiso) => permiso.id));
        },
        error: () => (this.mensajeError = 'No se pudo cargar el rol.')
      });
  }

  private capitalizar(valor: string): string {
    return valor.charAt(0).toUpperCase() + valor.slice(1);
  }
}
