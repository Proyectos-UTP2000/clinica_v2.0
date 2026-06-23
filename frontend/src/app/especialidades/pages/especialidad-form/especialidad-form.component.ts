import { Component, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { finalize, forkJoin } from 'rxjs';
import { EspecialidadResponse } from '../../../shared/models/especialidad.model';
import { EspecialidadService } from '../../services/especialidad.service';
import { ApiErrorService } from '../../../core/services/api-error.service';

@Component({
    selector: 'app-especialidad-form',
    templateUrl: './especialidad-form.component.html',
    standalone: false
})
export class EspecialidadFormComponent implements OnInit {
  especialidadId: number | null = null;
  especialidades: EspecialidadResponse[] = [];
  soloPrincipalesDropdown = true;
  cargando = false;
  guardando = false;
  mensajeError = '';

  especialidadForm = this.fb.group({
    nombre: ['', Validators.required],
    descripcion: [''],
    especialidadPadreId: [null as number | null]
  });

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly fb: FormBuilder,
    private readonly especialidadService: EspecialidadService,
    private readonly apiErrorService: ApiErrorService
  ) {}

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    this.especialidadId = idParam ? Number(idParam) : null;
    this.cargarDatos();
  }

  guardar(): void {
    if (this.especialidadForm.invalid) {
      this.especialidadForm.markAllAsTouched();
      return;
    }
    const request = {
      nombre: this.especialidadForm.value.nombre || '',
      descripcion: this.especialidadForm.value.descripcion || undefined,
      especialidadPadreId: this.especialidadForm.value.especialidadPadreId || null
    };
    this.guardando = true;
    this.mensajeError = '';
    const operacion = this.especialidadId
      ? this.especialidadService.actualizar(this.especialidadId, request)
      : this.especialidadService.crear(request);
    operacion.pipe(finalize(() => (this.guardando = false))).subscribe({
      next: () => this.router.navigate(['/especialidades']),
      error: (err) => (this.mensajeError = this.apiErrorService.obtenerMensajeError(err))
    });
  }

  get especialidadesPadreDisponibles(): EspecialidadResponse[] {
    if (this.soloPrincipalesDropdown) {
      return this.especialidades.filter(e => !e.especialidadPadreId);
    }
    return this.especialidades;
  }

  private cargarDatos(): void {
    this.cargando = true;
    const detalle$ = this.especialidadId ? this.especialidadService.obtener(this.especialidadId) : undefined;
    const datos$ = detalle$
      ? forkJoin({ especialidades: this.especialidadService.listarTodas(), detalle: detalle$ })
      : forkJoin({ especialidades: this.especialidadService.listarTodas() });
    datos$.pipe(finalize(() => (this.cargando = false))).subscribe({
      next: (datos) => {
        this.especialidades = datos.especialidades.filter((especialidad) => especialidad.id !== this.especialidadId);
        if ('detalle' in datos) {
          const detalle = datos.detalle as EspecialidadResponse;
          this.especialidadForm.patchValue({
            nombre: detalle.nombre,
            descripcion: detalle.descripcion || '',
            especialidadPadreId: detalle.especialidadPadreId || null
          });
        }
      },
      error: (err) => (this.mensajeError = this.apiErrorService.obtenerMensajeError(err))
    });
  }
}
