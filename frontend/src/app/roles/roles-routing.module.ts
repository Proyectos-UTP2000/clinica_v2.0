import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ListarRolesComponent } from './pages/listar-roles/listar-roles.component';
import { RolFormComponent } from './pages/rol-form/rol-form.component';

const routes: Routes = [
  { path: '', component: ListarRolesComponent },
  { path: 'crear', component: RolFormComponent },
  { path: 'editar/:id', component: RolFormComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class RolesRoutingModule {}
