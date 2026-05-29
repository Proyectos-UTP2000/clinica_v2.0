import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { CrearMedicoComponent } from './pages/crear-medico/crear-medico.component';
import { EditarMedicoComponent } from './pages/editar-medico/editar-medico.component';
import { ListarMedicosComponent } from './pages/listar-medicos/listar-medicos.component';

const routes: Routes = [
  { path: '', component: ListarMedicosComponent },
  { path: 'crear', component: CrearMedicoComponent },
  { path: 'editar/:id', component: EditarMedicoComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class MedicosRoutingModule {}
