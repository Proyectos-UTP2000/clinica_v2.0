import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot, UrlTree } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {
  constructor(
    private readonly authService: AuthService,
    private readonly router: Router
  ) {}

  canActivate(_route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean | UrlTree {
    if (!this.authService.isLoggedIn()) {
      return this.router.createUrlTree(['/login']);
    }

    const usuario = this.authService.getUsuarioAutenticado();
    const requiereCambio = usuario?.cambioPasswordObligatorio === true;
    const estaEnCambioPassword = state.url.startsWith('/cambiar-password');

    if (requiereCambio && !estaEnCambioPassword) {
      return this.router.createUrlTree(['/cambiar-password']);
    }

    return true;
  }
}
