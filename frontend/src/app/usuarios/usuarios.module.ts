import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { SharedModule } from '../shared/shared.module';
import { ListarUsuariosComponent } from './pages/listar-usuarios/listar-usuarios.component';
import { UsuarioFormComponent } from './pages/usuario-form/usuario-form.component';
import { BitacoraComponent } from './pages/bitacora/bitacora.component';
import { UsuariosRoutingModule } from './usuarios-routing.module';
import { UsuarioService } from './services/usuario.service';

@NgModule({
  declarations: [ListarUsuariosComponent, UsuarioFormComponent, BitacoraComponent],
  imports: [CommonModule, FormsModule, ReactiveFormsModule, SharedModule, UsuariosRoutingModule],
  providers: [UsuarioService]
})
export class UsuariosModule {}
