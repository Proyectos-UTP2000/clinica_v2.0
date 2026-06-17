import { provideHttpClient } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { AuthService } from './auth.service';

describe('AuthService permisos efectivos', () => {
  let service: AuthService;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [AuthService, provideHttpClient()]
    });
    service = TestBed.inject(AuthService);
  });

  afterEach(() => localStorage.clear());

  it('no concede permisos solo por tener rol Administrador', () => {
    localStorage.setItem('clinica_usuario', JSON.stringify({
      token: 'token',
      dni: '00000000',
      roles: ['Administrador'],
      permisos: []
    }));

    expect(service.hasPermission('pacientes.crear')).toBeFalse();
  });

  it('concede permisos por codigo explicito', () => {
    localStorage.setItem('clinica_usuario', JSON.stringify({
      token: 'token',
      dni: '00000000',
      roles: ['Administrador'],
      permisos: ['pacientes.crear']
    }));

    expect(service.hasPermission('pacientes.crear')).toBeTrue();
  });
});
