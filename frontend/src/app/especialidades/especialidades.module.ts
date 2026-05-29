import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { SharedModule } from '../shared/shared.module';
import { EspecialidadesRoutingModule } from './especialidades-routing.module';
import { EspecialidadFormComponent } from './pages/especialidad-form/especialidad-form.component';
import { ListarEspecialidadesComponent } from './pages/listar-especialidades/listar-especialidades.component';
import { EspecialidadService } from './services/especialidad.service';

@NgModule({
  declarations: [ListarEspecialidadesComponent, EspecialidadFormComponent],
  imports: [CommonModule, ReactiveFormsModule, SharedModule, EspecialidadesRoutingModule],
  providers: [EspecialidadService]
})
export class EspecialidadesModule {}
