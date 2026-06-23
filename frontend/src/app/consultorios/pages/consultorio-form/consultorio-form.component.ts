import { Component, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { finalize, forkJoin, of } from 'rxjs';
import { SedeResponse } from '../../../shared/models/sede.model';
import { SedeService } from '../../../sedes/services/sede.service';
import { ConsultorioService } from '../../services/consultorio.service';
import { ApiErrorService } from '../../../core/services/api-error.service';

@Component({
  selector: 'app-consultorio-form',
  templateUrl: './consultorio-form.component.html',
  standalone: false
})
export class ConsultorioFormComponent implements OnInit {
  consultorioId: number | null = null;
  sedes: SedeResponse[] = [];
  cargando = false;
  guardando = false;
  mensajeError = '';

  consultorioForm = this.fb.group({
    sedeId: [null as number | null, Validators.required],
    nombre: ['', [Validators.required, Validators.maxLength(50)]],
    piso: ['', Validators.maxLength(10)],
    area: ['', Validators.maxLength(50)]
  });

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly fb: FormBuilder,
    private readonly consultorioService: ConsultorioService,
    private readonly sedeService: SedeService,
    private readonly apiErrorService: ApiErrorService
  ) {}

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    this.consultorioId = idParam ? Number(idParam) : null;
    this.cargarDatos();
  }

  guardar(): void {
    if (this.consultorioForm.invalid) {
      this.consultorioForm.markAllAsTouched();
      return;
    }

    const request = {
      sedeId: this.consultorioForm.value.sedeId ? Number(this.consultorioForm.value.sedeId) : 0,
      nombre: this.consultorioForm.value.nombre || '',
      piso: this.consultorioForm.value.piso || undefined,
      area: this.consultorioForm.value.area || undefined
    };

    this.guardando = true;
    this.mensajeError = '';

    const operacion = this.consultorioId
      ? this.consultorioService.actualizar(this.consultorioId, {
          nombre: request.nombre,
          piso: request.piso,
          area: request.area
        })
      : this.consultorioService.crear(request);

    operacion.pipe(finalize(() => (this.guardando = false))).subscribe({
      next: () => this.router.navigate(['/consultorios']),
      error: (err) => (this.mensajeError = this.apiErrorService.obtenerMensajeError(err))
    });
  }

  campoInvalido(campo: string): boolean {
    const control = this.consultorioForm.get(campo);
    return Boolean(control?.invalid && (control.dirty || control.touched));
  }

  private cargarDatos(): void {
    this.cargando = true;
    this.mensajeError = '';

    const sedes$ = this.sedeService.listarTodas();
    const detalle$ = this.consultorioId ? this.consultorioService.obtener(this.consultorioId) : of(null);

    forkJoin({ sedes: sedes$, detalle: detalle$ })
      .pipe(finalize(() => (this.cargando = false)))
      .subscribe({
        next: (datos) => {
          this.sedes = datos.sedes;
          if (datos.detalle) {
            const det = datos.detalle;
            this.consultorioForm.patchValue({
              sedeId: det.sedeId,
              nombre: det.nombre,
              piso: det.piso || '',
              area: det.area || ''
            });
            this.consultorioForm.get('sedeId')?.disable();
          }
        },
        error: (err) => (this.mensajeError = this.apiErrorService.obtenerMensajeError(err))
      });
  }
}
