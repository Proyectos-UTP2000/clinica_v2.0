import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AgendaCalendarComponent } from './pages/agenda-calendar/agenda-calendar.component';

const routes: Routes = [
  { path: '', component: AgendaCalendarComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class AgendaRoutingModule {}
