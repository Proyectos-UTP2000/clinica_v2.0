import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { SharedModule } from '../shared/shared.module';
import { CitasRoutingModule } from './citas-routing.module';
import { CrearCitaComponent } from './pages/crear-cita/crear-cita.component';
import { ListarCitasComponent } from './pages/listar-citas/listar-citas.component';
import { CitaService } from './services/cita.service';

@NgModule({
  declarations: [
    ListarCitasComponent,
    CrearCitaComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    SharedModule,
    CitasRoutingModule
  ],
  providers: [
    CitaService
  ]
})
export class CitasModule {}
