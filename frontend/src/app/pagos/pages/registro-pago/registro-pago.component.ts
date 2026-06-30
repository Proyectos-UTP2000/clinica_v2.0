import { Component, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { finalize } from 'rxjs';
import { CitaResponse } from '../../../shared/models/cita.model';
import { PagoService } from '../../services/pago.service';

@Component({
    selector: 'app-registro-pago',
    templateUrl: './registro-pago.component.html',
    standalone: false
})
export class RegistroPagoComponent implements OnInit {
  cita?: CitaResponse;
  citaId = 0;
  cargando = false;
  guardando = false;
  mensajeError = '';
  cajaCerrada = false;

  pagoForm = this.fb.group({
    monto: [80, [Validators.required, Validators.min(0.01)]],
    metodo: ['efectivo', Validators.required]
  });

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly fb: FormBuilder,
    private readonly pagoService: PagoService
  ) {}

  ngOnInit(): void {
    this.citaId = Number(this.route.snapshot.paramMap.get('citaId'));
    this.cargarCita();
  }

  registrar(): void {
    if (this.pagoForm.invalid) {
      this.pagoForm.markAllAsTouched();
      return;
    }
    this.guardando = true;
    this.mensajeError = '';
    this.pagoService.registrar({
      citaId: this.citaId,
      monto: Number(this.pagoForm.value.monto),
      metodo: this.pagoForm.value.metodo || 'efectivo'
    }).pipe(finalize(() => (this.guardando = false))).subscribe({
      next: () => this.router.navigate(['/pagos']),
      error: (err: any) => {
        const msg = err.error?.mensaje || err.error?.message || '';
        if (msg.toLowerCase().includes('caja')) {
          this.cajaCerrada = true;
          this.mensajeError = 'Se debe de abrir la caja primero.';
        } else {
          this.cajaCerrada = false;
          this.mensajeError = msg || 'No se pudo registrar el pago.';
        }
      }
    });
  }

  private cargarCita(): void {
    this.cargando = true;
    this.pagoService.obtenerCita(this.citaId)
      .pipe(finalize(() => (this.cargando = false)))
      .subscribe({
        next: (cita) => (this.cita = cita),
        error: () => (this.mensajeError = 'No se pudo cargar la cita.')
      });
  }
}
