import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthGuard } from './core/guards/auth.guard';
import { LayoutComponent } from './layout/layout.component';

const routes: Routes = [
  {
    path: '',
    component: LayoutComponent,
    canActivate: [AuthGuard],
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
      {
        path: 'dashboard',
        loadChildren: () => import('./dashboard/dashboard.module').then((m) => m.DashboardModule)
      },
      { path: 'pacientes', redirectTo: 'dashboard' },
      { path: 'medicos', redirectTo: 'dashboard' },
      { path: 'citas', redirectTo: 'dashboard' },
      { path: 'historial', redirectTo: 'dashboard' },
      { path: 'pagos', redirectTo: 'dashboard' },
      { path: 'sedes', redirectTo: 'dashboard' },
      { path: 'especialidades', redirectTo: 'dashboard' }
    ]
  },
  { path: '**', redirectTo: 'dashboard' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
