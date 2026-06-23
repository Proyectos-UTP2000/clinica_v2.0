import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ListarConsultoriosComponent } from './pages/listar-consultorios/listar-consultorios.component';
import { ConsultorioFormComponent } from './pages/consultorio-form/consultorio-form.component';

const routes: Routes = [
  { path: '', component: ListarConsultoriosComponent },
  { path: 'crear', component: ConsultorioFormComponent },
  { path: 'editar/:id', component: ConsultorioFormComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class ConsultoriosRoutingModule {}
