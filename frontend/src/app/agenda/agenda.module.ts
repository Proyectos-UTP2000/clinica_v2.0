import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { CitaService } from '../citas/services/cita.service';
import { SharedModule } from '../shared/shared.module';
import { AgendaRoutingModule } from './agenda-routing.module';
import { AgendaCalendarComponent } from './pages/agenda-calendar/agenda-calendar.component';
import { AgendaService } from './services/agenda.service';

@NgModule({
  declarations: [AgendaCalendarComponent],
  imports: [CommonModule, FormsModule, ReactiveFormsModule, SharedModule, AgendaRoutingModule],
  providers: [AgendaService, CitaService]
})
export class AgendaModule {}
