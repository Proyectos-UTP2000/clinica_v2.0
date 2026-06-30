import { Component, OnInit } from '@angular/core';
import { finalize } from 'rxjs';
import { PagoService } from '../../services/pago.service';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-gestion-caja',
  templateUrl: './gestion-caja.component.html',
  styleUrl: './gestion-caja.component.css',
  standalone: false
})
export class GestionCajaComponent implements OnInit {
  caja: any = null;
  pagos: any[] = [];
  cargando = true;
  abriendo = false;
  cerrando = false;
  procesandoPdf = false;
  mensajeError = '';
  mensajeExito = '';

  // Form fields
  montoApertura = 0;
  balanceReal = 0;
  observaciones = '';

  constructor(
    public readonly authService: AuthService,
    private readonly pagoService: PagoService
  ) {}

  ngOnInit(): void {
    this.cargarCajaHoy();
  }

  cargarCajaHoy(): void {
    this.cargando = true;
    this.mensajeError = '';
    this.pagoService.obtenerCajaHoy()
      .pipe(finalize(() => this.cargando = false))
      .subscribe({
        next: (cajaData) => {
          this.caja = cajaData;
          this.cargarPagosDeCaja(cajaData.id);
        },
        error: (err) => {
          // Si es 404, significa que no está abierta, no es un error crítico
          if (err.status === 404) {
            this.caja = null;
          } else {
            this.mensajeError = 'Error al obtener el estado de la caja de hoy.';
          }
        }
      });
  }

  cargarPagosDeCaja(cajaId: number): void {
    this.pagoService.listarPorCaja(cajaId).subscribe({
      next: (pagosList) => {
        this.pagos = pagosList;
      },
      error: () => {
        this.mensajeError = 'No se pudieron cargar las transacciones del día.';
      }
    });
  }

  abrirCaja(): void {
    if (this.montoApertura < 0) {
      this.mensajeError = 'El monto de apertura no puede ser negativo.';
      return;
    }
    this.abriendo = true;
    this.mensajeError = '';
    this.mensajeExito = '';
    this.pagoService.abrirCaja(this.montoApertura, this.observaciones)
      .pipe(finalize(() => this.abriendo = false))
      .subscribe({
        next: (cajaData) => {
          this.caja = cajaData;
          this.mensajeExito = 'Caja abierta correctamente para el día de hoy.';
          this.observaciones = '';
          this.pagos = [];
        },
        error: (err) => {
          this.mensajeError = err.error?.mensaje || 'Error al intentar abrir la caja.';
        }
      });
  }

  cerrarCaja(): void {
    if (this.balanceReal < 0) {
      this.mensajeError = 'El balance real no puede ser negativo.';
      return;
    }
    this.cerrando = true;
    this.mensajeError = '';
    this.mensajeExito = '';
    this.pagoService.cerrarCaja(this.balanceReal, this.observaciones)
      .pipe(finalize(() => this.cerrando = false))
      .subscribe({
        next: (cajaData) => {
          this.caja = cajaData;
          this.mensajeExito = 'Caja cerrada correctamente. Balance guardado.';
          this.observaciones = '';
          this.cargarPagosDeCaja(cajaData.id);
        },
        error: (err) => {
          this.mensajeError = err.error?.mensaje || 'Error al intentar cerrar la caja.';
        }
      });
  }

  descargarReportePdf(): void {
    if (!this.caja) return;
    this.procesandoPdf = true;
    this.pagoService.descargarReportePdf(this.caja.id)
      .pipe(finalize(() => this.procesandoPdf = false))
      .subscribe({
        next: (blob) => {
          const url = window.URL.createObjectURL(blob);
          const a = document.createElement('a');
          a.href = url;
          a.download = `reporte_caja_${this.caja.fecha}.pdf`;
          document.body.appendChild(a);
          a.click();
          document.body.removeChild(a);
          window.URL.revokeObjectURL(url);
        },
        error: () => {
          this.mensajeError = 'Error al intentar descargar el reporte en PDF.';
        }
      });
  }

  get diferenciaClase(): string {
    if (!this.caja || this.caja.diferencia === undefined) return '';
    if (this.caja.diferencia === 0) return 'text-success fw-bold';
    return 'text-danger fw-bold';
  }

  get finalEsperado(): number {
    if (!this.caja) return 0;
    return (this.caja.montoApertura || 0) + (this.caja.ingresos || 0) - (this.caja.egresos || 0);
  }
}
