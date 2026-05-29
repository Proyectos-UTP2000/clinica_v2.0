import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { CrearCitaComponent } from './pages/crear-cita/crear-cita.component';
import { ListarCitasComponent } from './pages/listar-citas/listar-citas.component';

const routes: Routes = [
  { path: '', component: ListarCitasComponent },
  { path: 'crear', component: CrearCitaComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class CitasRoutingModule {}
