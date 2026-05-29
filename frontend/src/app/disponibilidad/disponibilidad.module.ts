import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { SharedModule } from '../shared/shared.module';
import { DisponibilidadRoutingModule } from './disponibilidad-routing.module';
import { GestionDisponibilidadComponent } from './pages/gestion-disponibilidad/gestion-disponibilidad.component';
import { DisponibilidadService } from './services/disponibilidad.service';

@NgModule({
  declarations: [GestionDisponibilidadComponent],
  imports: [CommonModule, FormsModule, ReactiveFormsModule, SharedModule, DisponibilidadRoutingModule],
  providers: [DisponibilidadService]
})
export class DisponibilidadModule {}
