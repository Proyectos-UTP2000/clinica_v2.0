import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { SharedModule } from '../shared/shared.module';
import { ListarRolesComponent } from './pages/listar-roles/listar-roles.component';
import { RolFormComponent } from './pages/rol-form/rol-form.component';
import { RolesRoutingModule } from './roles-routing.module';
import { RolService } from './services/rol.service';

@NgModule({
  declarations: [ListarRolesComponent, RolFormComponent],
  imports: [CommonModule, FormsModule, ReactiveFormsModule, SharedModule, RolesRoutingModule],
  providers: [RolService]
})
export class RolesModule {}
