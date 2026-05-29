import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { CrearConsultaComponent } from './pages/crear-consulta/crear-consulta.component';
import { ListarHistorialComponent } from './pages/listar-historial/listar-historial.component';
import { VerConsultaComponent } from './pages/ver-consulta/ver-consulta.component';

const routes: Routes = [
  { path: '', component: ListarHistorialComponent },
  { path: 'paciente/:pacienteId', component: ListarHistorialComponent },
  { path: 'crear', component: CrearConsultaComponent },
  { path: 'ver/:id', component: VerConsultaComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class HistorialRoutingModule {}
