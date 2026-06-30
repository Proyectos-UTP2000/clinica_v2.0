import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ListarUsuariosComponent } from './pages/listar-usuarios/listar-usuarios.component';
import { UsuarioFormComponent } from './pages/usuario-form/usuario-form.component';
import { BitacoraComponent } from './pages/bitacora/bitacora.component';

const routes: Routes = [
  { path: '', component: ListarUsuariosComponent },
  { path: 'bitacora', component: BitacoraComponent },
  { path: 'crear', component: UsuarioFormComponent },
  { path: 'editar/:id', component: UsuarioFormComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class UsuariosRoutingModule {}
