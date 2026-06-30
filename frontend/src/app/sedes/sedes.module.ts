import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { SharedModule } from '../shared/shared.module';
import { ListarSedesComponent } from './pages/listar-sedes/listar-sedes.component';
import { SedeFormComponent } from './pages/sede-form/sede-form.component';
import { ConfiguracionGlobalComponent } from './pages/configuracion-global/configuracion-global.component';
import { SedesRoutingModule } from './sedes-routing.module';
import { SedeService } from './services/sede.service';

@NgModule({
  declarations: [ListarSedesComponent, SedeFormComponent, ConfiguracionGlobalComponent],
  imports: [CommonModule, FormsModule, ReactiveFormsModule, SharedModule, SedesRoutingModule],
  providers: [SedeService]
})
export class SedesModule {}
