import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { CrearPacienteComponent } from './pages/crear-paciente/crear-paciente.component';
import { EditarPacienteComponent } from './pages/editar-paciente/editar-paciente.component';
import { ListarPacientesComponent } from './pages/listar-pacientes/listar-pacientes.component';

const routes: Routes = [
  { path: '', component: ListarPacientesComponent },
  { path: 'crear', component: CrearPacienteComponent },
  { path: 'editar/:id', component: EditarPacienteComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class PacientesRoutingModule {}
