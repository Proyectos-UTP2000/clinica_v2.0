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
      {
        path: 'pacientes',
        loadChildren: () => import('./pacientes/pacientes.module').then((m) => m.PacientesModule)
      },
      {
        path: 'medicos',
        loadChildren: () => import('./medicos/medicos.module').then((m) => m.MedicosModule)
      },
      {
        path: 'citas',
        loadChildren: () => import('./citas/citas.module').then((m) => m.CitasModule)
      },
      {
        path: 'agenda',
        loadChildren: () => import('./agenda/agenda.module').then((m) => m.AgendaModule)
      },
      {
        path: 'historial',
        loadChildren: () => import('./historial/historial.module').then((m) => m.HistorialModule)
      },
      {
        path: 'pagos',
        loadChildren: () => import('./pagos/pagos.module').then((m) => m.PagosModule)
      },
      {
        path: 'sedes',
        loadChildren: () => import('./sedes/sedes.module').then((m) => m.SedesModule)
      },
      {
        path: 'roles',
        loadChildren: () => import('./roles/roles.module').then((m) => m.RolesModule)
      },
      {
        path: 'usuarios',
        loadChildren: () => import('./usuarios/usuarios.module').then((m) => m.UsuariosModule)
      },
      {
        path: 'especialidades',
        loadChildren: () => import('./especialidades/especialidades.module').then((m) => m.EspecialidadesModule)
      },
      {
        path: 'disponibilidad',
        loadChildren: () => import('./disponibilidad/disponibilidad.module').then((m) => m.DisponibilidadModule)
      }
    ]
  },
  { path: '**', redirectTo: 'dashboard' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
