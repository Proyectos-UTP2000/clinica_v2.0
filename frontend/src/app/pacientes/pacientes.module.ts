import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { SharedModule } from '../shared/shared.module';
import { PacientesRoutingModule } from './pacientes-routing.module';
import { CrearPacienteComponent } from './pages/crear-paciente/crear-paciente.component';
import { EditarPacienteComponent } from './pages/editar-paciente/editar-paciente.component';
import { ListarPacientesComponent } from './pages/listar-pacientes/listar-pacientes.component';
import { PacienteService } from './services/paciente.service';

@NgModule({
  declarations: [
    ListarPacientesComponent,
    CrearPacienteComponent,
    EditarPacienteComponent
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    SharedModule,
    PacientesRoutingModule
  ],
  providers: [
    PacienteService
  ]
})
export class PacientesModule {}
