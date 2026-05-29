import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { SharedModule } from '../shared/shared.module';
import { HistorialRoutingModule } from './historial-routing.module';
import { CrearConsultaComponent } from './pages/crear-consulta/crear-consulta.component';
import { ListarHistorialComponent } from './pages/listar-historial/listar-historial.component';
import { VerConsultaComponent } from './pages/ver-consulta/ver-consulta.component';
import { HistorialService } from './services/historial.service';

@NgModule({
  declarations: [ListarHistorialComponent, CrearConsultaComponent, VerConsultaComponent],
  imports: [CommonModule, ReactiveFormsModule, SharedModule, HistorialRoutingModule],
  providers: [HistorialService]
})
export class HistorialModule {}
