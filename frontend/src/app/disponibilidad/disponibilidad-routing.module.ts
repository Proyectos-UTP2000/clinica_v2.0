import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { GestionDisponibilidadComponent } from './pages/gestion-disponibilidad/gestion-disponibilidad.component';

const routes: Routes = [
  { path: '', component: GestionDisponibilidadComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class DisponibilidadRoutingModule {}
