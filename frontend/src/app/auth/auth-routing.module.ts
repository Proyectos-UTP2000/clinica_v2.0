import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthGuard } from '../core/guards/auth.guard';
import { NoAuthGuard } from '../core/guards/no-auth.guard';
import { CambiarPasswordComponent } from './cambiar-password/cambiar-password.component';
import { LoginComponent } from './login/login.component';
import { RecuperarPasswordComponent } from './recuperar-password/recuperar-password.component';
import { MiPerfilComponent } from './mi-perfil/mi-perfil.component';

const routes: Routes = [
  { path: 'login', component: LoginComponent, canActivate: [NoAuthGuard] },
  { path: 'cambiar-password', component: CambiarPasswordComponent, canActivate: [AuthGuard] },
  { path: 'recuperar-password', component: RecuperarPasswordComponent, canActivate: [NoAuthGuard] },
  { path: 'mi-perfil', component: MiPerfilComponent, canActivate: [AuthGuard] }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class AuthRoutingModule {}
