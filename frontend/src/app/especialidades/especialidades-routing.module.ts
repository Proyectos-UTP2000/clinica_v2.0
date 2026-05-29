import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { EspecialidadFormComponent } from './pages/especialidad-form/especialidad-form.component';
import { ListarEspecialidadesComponent } from './pages/listar-especialidades/listar-especialidades.component';

const routes: Routes = [
  { path: '', component: ListarEspecialidadesComponent },
  { path: 'crear', component: EspecialidadFormComponent },
  { path: 'editar/:id', component: EspecialidadFormComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class EspecialidadesRoutingModule {}
