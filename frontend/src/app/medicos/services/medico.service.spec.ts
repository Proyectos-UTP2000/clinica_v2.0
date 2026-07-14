import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { MedicoService } from './medico.service';

const API_URL = '/api';

describe('MedicoService', () => {
  let service: MedicoService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [MedicoService, provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(MedicoService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('busca medicos por sede y especialidad usando el endpoint dedicado', () => {
    service.buscar({ sedeId: 2, especialidadId: 3 }).subscribe((medicos) => {
      expect(medicos.length).toBe(1);
      expect(medicos[0].nombres).toBe('Maria');
    });

    const req = http.expectOne(`${API_URL}/medicos/buscar?page=0&size=100&especialidadId=3&sedeId=2`);
    expect(req.request.method).toBe('GET');
    req.flush({
      content: [{
        id: 4,
        usuarioId: 10,
        dni: '12345678',
        nombres: 'Maria',
        apellidos: 'Lopez',
        email: 'maria@example.com',
        especialidadNombre: 'Pediatría',
        sedes: ['Central'],
        activo: true
      }],
      totalElements: 1,
      totalPages: 1,
      size: 100,
      number: 0
    });
  });

  it('consulta datos externos para autocompletar DNI', () => {
    service.consultarDni('87654321').subscribe((info) => {
      expect(info.nombres).toBe('CARLOS');
      expect(info.apellidos).toBe('RAMOS VEGA');
    });

    const req = http.expectOne(`${API_URL}/medicos/buscar-dni?dni=87654321`);
    expect(req.request.method).toBe('GET');
    req.flush({ dni: '87654321', nombres: 'CARLOS', apellidos: 'RAMOS VEGA' });
  });
});
