import { Component, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { finalize } from 'rxjs';
import { PermisoResponse } from '../../../shared/models/rol.model';
import { RolService } from '../../services/rol.service';

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

  rolForm = this.fb.group({
    nombre: ['', Validators.required],
    descripcion: [''],
    activo: [true]
  });

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly fb: FormBuilder,
    private readonly rolService: RolService
  ) {}

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    this.rolId = idParam ? Number(idParam) : null;
    this.cargarPermisos();
    if (this.rolId) {
      this.cargarRol(this.rolId);
    }
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
    operacion.pipe(finalize(() => (this.guardando = false))).subscribe({
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
}
