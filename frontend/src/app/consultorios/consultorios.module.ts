import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { SharedModule } from '../shared/shared.module';
import { ListarConsultoriosComponent } from './pages/listar-consultorios/listar-consultorios.component';
import { ConsultorioFormComponent } from './pages/consultorio-form/consultorio-form.component';
import { ConsultoriosRoutingModule } from './consultorios-routing.module';
import { ConsultorioService } from './services/consultorio.service';

@NgModule({
  declarations: [ListarConsultoriosComponent, ConsultorioFormComponent],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    SharedModule,
    ConsultoriosRoutingModule
  ],
  providers: [ConsultorioService]
})
export class ConsultoriosModule {}
