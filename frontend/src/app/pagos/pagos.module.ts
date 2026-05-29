import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { SharedModule } from '../shared/shared.module';
import { HistorialPagosComponent } from './pages/historial-pagos/historial-pagos.component';
import { ListarPagosComponent } from './pages/listar-pagos/listar-pagos.component';
import { RegistroPagoComponent } from './pages/registro-pago/registro-pago.component';
import { PagosRoutingModule } from './pagos-routing.module';
import { PagoService } from './services/pago.service';

@NgModule({
  declarations: [ListarPagosComponent, RegistroPagoComponent, HistorialPagosComponent],
  imports: [CommonModule, ReactiveFormsModule, SharedModule, PagosRoutingModule],
  providers: [PagoService]
})
export class PagosModule {}
