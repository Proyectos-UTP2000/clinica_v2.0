import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { AuthService } from './auth.service';

const API_URL = 'http://localhost:8080/api';

describe('AuthService permisos efectivos', () => {
  let service: AuthService;
  let http: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [AuthService, provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(AuthService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    http.verify();
    localStorage.clear();
  });

  it('no concede permisos solo por tener rol Administrador', () => {
    localStorage.setItem('clinica_usuario', JSON.stringify({
      token: 'token',
      dni: '00000000',
      nombres: 'Ada',
      apellidos: 'Lovelace',
      roles: ['Administrador'],
      permisos: []
    }));

    expect(service.hasPermission('pacientes.crear')).toBeFalse();
  });

  it('concede permisos por codigo explicito', () => {
    localStorage.setItem('clinica_usuario', JSON.stringify({
      token: 'token',
      dni: '00000000',
      nombres: 'Ada',
      apellidos: 'Lovelace',
      roles: ['Administrador'],
      permisos: ['pacientes.crear']
    }));

    expect(service.hasPermission('pacientes.crear')).toBeTrue();
  });

  it('refresca la sesion desde /auth/me y actualiza localStorage', () => {
    localStorage.setItem('clinica_usuario', JSON.stringify({
      token: 'token-viejo',
      dni: '00000000',
      nombres: 'Ada',
      apellidos: 'Lovelace',
      roles: ['Administrador'],
      permisos: ['pacientes.crear']
    }));

    service.refrescarSesion().subscribe((usuario) => {
      expect(usuario.token).toBe('token-nuevo');
      expect(usuario.permisos).toEqual(['dashboard.ver']);
      expect(JSON.parse(localStorage.getItem('clinica_usuario') || '{}').permisos).toEqual(['dashboard.ver']);
      expect(localStorage.getItem('clinica_token')).toBe('token-nuevo');
    });

    const req = http.expectOne(`${API_URL}/auth/me`);
    expect(req.request.method).toBe('GET');
    req.flush({
      token: 'token-nuevo',
      dni: '00000000',
      nombres: 'Ada',
      apellidos: 'Lovelace',
      roles: ['Administrador'],
      permisos: ['dashboard.ver'],
      cambioPasswordObligatorio: false
    });
  });
});
