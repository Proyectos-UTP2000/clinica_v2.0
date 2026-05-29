import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { HistorialPagosComponent } from './pages/historial-pagos/historial-pagos.component';
import { ListarPagosComponent } from './pages/listar-pagos/listar-pagos.component';
import { RegistroPagoComponent } from './pages/registro-pago/registro-pago.component';

const routes: Routes = [
  { path: '', component: ListarPagosComponent },
  { path: 'cita/:citaId', component: RegistroPagoComponent },
  { path: 'paciente/:pacienteId', component: HistorialPagosComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class PagosRoutingModule {}
