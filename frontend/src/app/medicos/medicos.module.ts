import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { SharedModule } from '../shared/shared.module';
import { MedicosRoutingModule } from './medicos-routing.module';
import { CrearMedicoComponent } from './pages/crear-medico/crear-medico.component';
import { EditarMedicoComponent } from './pages/editar-medico/editar-medico.component';
import { ListarMedicosComponent } from './pages/listar-medicos/listar-medicos.component';
import { MedicoService } from './services/medico.service';

@NgModule({
  declarations: [
    ListarMedicosComponent,
    CrearMedicoComponent,
    EditarMedicoComponent
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    SharedModule,
    MedicosRoutingModule
  ],
  providers: [
    MedicoService
  ]
})
export class MedicosModule {}
