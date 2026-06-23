import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { PacienteService } from './paciente.service';

const API_URL = 'http://localhost:8080/api';

describe('PacienteService', () => {
  let service: PacienteService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [PacienteService, provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(PacienteService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('busca paciente existente por DNI', () => {
    service.buscarPorDni('12345678').subscribe((paciente) => {
      expect(paciente.dni).toBe('12345678');
    });

    const req = http.expectOne(`${API_URL}/pacientes/buscar?dni=12345678`);
    expect(req.request.method).toBe('GET');
    req.flush({
      id: 1,
      dni: '12345678',
      nombres: 'Ana',
      apellidos: 'Rojas',
      fechaNacimiento: '1990-01-01',
      telefono: '999999999',
      activo: true
    });
  });

  it('consulta datos externos para autocompletar DNI', () => {
    service.consultarDni('87654321').subscribe((info) => {
      expect(info.nombres).toBe('LUIS');
      expect(info.apellidos).toBe('PEREZ DIAZ');
    });

    const req = http.expectOne(`${API_URL}/pacientes/buscar-dni?dni=87654321`);
    expect(req.request.method).toBe('GET');
    req.flush({ dni: '87654321', nombres: 'LUIS', apellidos: 'PEREZ DIAZ' });
  });
});
