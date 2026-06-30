import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ListarSedesComponent } from './pages/listar-sedes/listar-sedes.component';
import { SedeFormComponent } from './pages/sede-form/sede-form.component';
import { ConfiguracionGlobalComponent } from './pages/configuracion-global/configuracion-global.component';

const routes: Routes = [
  { path: '', component: ListarSedesComponent },
  { path: 'configuracion-global', component: ConfiguracionGlobalComponent },
  { path: 'crear', component: SedeFormComponent },
  { path: 'editar/:id', component: SedeFormComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class SedesRoutingModule {}
