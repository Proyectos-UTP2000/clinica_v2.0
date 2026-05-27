import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { ApiResponse } from '../../shared/models/api-response.model';
import { CambioPasswordRequest } from '../../shared/models/cambio-password-request.model';
import { JwtResponse } from '../../shared/models/jwt-response.model';
import { RecuperarPasswordRequest } from '../../shared/models/recuperar-password-request.model';
import { VerificarCodigoRequest } from '../../shared/models/verificar-codigo-request.model';

const API_URL = 'http://localhost:8080/api';
const TOKEN_KEY = 'clinica_token';
const USER_KEY = 'clinica_usuario';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private usuarioActual: JwtResponse | null = this.leerUsuarioGuardado();

  constructor(private readonly http: HttpClient) {}

  login(dni: string, password: string): Observable<JwtResponse> {
    return this.http.post<JwtResponse>(`${API_URL}/auth/login`, { dni, password }).pipe(
      tap((response) => this.guardarSesion(response))
    );
  }

  cambiarPassword(request: CambioPasswordRequest): Observable<ApiResponse> {
    return this.http.post<ApiResponse>(`${API_URL}/auth/cambiar-password`, request);
  }

  solicitarRecuperacion(dni: string, email: string): Observable<ApiResponse> {
    const request: RecuperarPasswordRequest = { dni, email };
    return this.http.post<ApiResponse>(`${API_URL}/auth/recuperar-password`, request);
  }

  restablecerPassword(email: string, codigo: string, nuevaPassword: string): Observable<ApiResponse> {
    const request: VerificarCodigoRequest = { email, codigo, nuevaPassword };
    return this.http.post<ApiResponse>(`${API_URL}/auth/verificar-codigo-recuperacion`, request);
  }

  logout(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    this.usuarioActual = null;
  }

  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  isLoggedIn(): boolean {
    return Boolean(this.getToken());
  }

  getUsuarioAutenticado(): JwtResponse | null {
    if (!this.usuarioActual) {
      this.usuarioActual = this.leerUsuarioGuardado();
    }
    return this.usuarioActual;
  }

  hasPermission(permission: string): boolean {
    return this.getUsuarioAutenticado()?.permisos?.includes(permission) ?? false;
  }

  hasRole(role: string): boolean {
    return this.getUsuarioAutenticado()?.roles?.includes(role) ?? false;
  }

  marcarPasswordActualizado(): void {
    const usuario = this.getUsuarioAutenticado();
    if (!usuario) {
      return;
    }

    this.usuarioActual = { ...usuario, cambioPasswordObligatorio: false };
    localStorage.setItem(USER_KEY, JSON.stringify(this.usuarioActual));
  }

  getNombreUsuario(): string {
    const usuario = this.getUsuarioAutenticado();
    if (!usuario) {
      return 'Usuario';
    }
    return `${usuario.nombres ?? ''} ${usuario.apellidos ?? ''}`.trim() || usuario.dni;
  }

  private guardarSesion(response: JwtResponse): void {
    localStorage.setItem(TOKEN_KEY, response.token);
    localStorage.setItem(USER_KEY, JSON.stringify(response));
    this.usuarioActual = response;
  }

  private leerUsuarioGuardado(): JwtResponse | null {
    const rawUser = localStorage.getItem(USER_KEY);
    if (!rawUser) {
      return null;
    }

    try {
      return JSON.parse(rawUser) as JwtResponse;
    } catch {
      localStorage.removeItem(USER_KEY);
      return null;
    }
  }
}
