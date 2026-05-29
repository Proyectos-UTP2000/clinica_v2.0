import { Component, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { finalize } from 'rxjs';
import { SedeService } from '../../services/sede.service';

@Component({
  selector: 'app-sede-form',
  templateUrl: './sede-form.component.html'
})
export class SedeFormComponent implements OnInit {
  sedeId: number | null = null;
  cargando = false;
  guardando = false;
  mensajeError = '';

  sedeForm = this.fb.group({
    nombre: ['', Validators.required],
    direccion: ['']
  });

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly fb: FormBuilder,
    private readonly sedeService: SedeService
  ) {}

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    this.sedeId = idParam ? Number(idParam) : null;
    if (this.sedeId) {
      this.cargarSede(this.sedeId);
    }
  }

  guardar(): void {
    if (this.sedeForm.invalid) {
      this.sedeForm.markAllAsTouched();
      return;
    }
    const request = {
      nombre: this.sedeForm.value.nombre || '',
      direccion: this.sedeForm.value.direccion || undefined
    };
    this.guardando = true;
    this.mensajeError = '';
    const operacion = this.sedeId
      ? this.sedeService.actualizar(this.sedeId, request)
      : this.sedeService.crear(request);
    operacion.pipe(finalize(() => (this.guardando = false))).subscribe({
      next: () => this.router.navigate(['/sedes']),
      error: () => (this.mensajeError = 'No se pudo guardar la sede.')
    });
  }

  private cargarSede(id: number): void {
    this.cargando = true;
    this.sedeService.obtener(id)
      .pipe(finalize(() => (this.cargando = false)))
      .subscribe({
        next: (sede) => this.sedeForm.patchValue({ nombre: sede.nombre, direccion: sede.direccion }),
        error: () => (this.mensajeError = 'No se pudo cargar la sede.')
      });
  }
}
