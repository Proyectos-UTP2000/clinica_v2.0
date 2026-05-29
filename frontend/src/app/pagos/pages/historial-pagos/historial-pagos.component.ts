import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { finalize } from 'rxjs';
import { PagoResponse } from '../../../shared/models/pago.model';
import { PagoService } from '../../services/pago.service';

@Component({
  selector: 'app-historial-pagos',
  templateUrl: './historial-pagos.component.html'
})
export class HistorialPagosComponent implements OnInit {
  pacienteId = 0;
  pagos: PagoResponse[] = [];
  cargando = false;
  mensajeError = '';

  constructor(
    private readonly route: ActivatedRoute,
    private readonly pagoService: PagoService
  ) {}

  ngOnInit(): void {
    this.pacienteId = Number(this.route.snapshot.paramMap.get('pacienteId'));
    this.cargarPagos();
  }

  cargarPagos(): void {
    this.cargando = true;
    this.mensajeError = '';
    this.pagoService.listarPorPaciente(this.pacienteId)
      .pipe(finalize(() => (this.cargando = false)))
      .subscribe({
        next: (pagos) => (this.pagos = pagos),
        error: () => (this.mensajeError = 'No se pudieron cargar los pagos.')
      });
  }
}
