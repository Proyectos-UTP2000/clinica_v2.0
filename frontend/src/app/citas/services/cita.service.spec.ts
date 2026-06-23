import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { CitaService } from './cita.service';

const API_URL = 'http://localhost:8080/api';

describe('CitaService', () => {
  let service: CitaService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [CitaService, provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(CitaService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('reprograma cita enviando doctorId cuando se cambia de medico', () => {
    service.reprogramar(5, '2026-07-10T09:00', 8).subscribe((cita) => {
      expect(cita.id).toBe(5);
    });

    const req = http.expectOne(`${API_URL}/citas/5/reprogramar`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual({ nuevaFechaHora: '2026-07-10T09:00', doctorId: 8 });
    req.flush({
      id: 5,
      pacienteNombre: 'Ana Rojas',
      doctorNombre: 'Maria Lopez',
      sedeNombre: 'Central',
      fechaHoraInicio: '2026-07-10T09:00:00',
      fechaHoraFin: '2026-07-10T09:30:00',
      estado: 'programada',
      estadoPago: 'pendiente',
      origen: 'interno'
    });
  });

  it('mantiene el medico actual cuando no recibe doctorId', () => {
    service.reprogramar(6, '2026-07-11T10:00').subscribe((cita) => {
      expect(cita.id).toBe(6);
    });

    const req = http.expectOne(`${API_URL}/citas/6/reprogramar`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual({ nuevaFechaHora: '2026-07-11T10:00' });
    req.flush({
      id: 6,
      pacienteNombre: 'Ana Rojas',
      doctorNombre: 'Luis Perez',
      sedeNombre: 'Central',
      fechaHoraInicio: '2026-07-11T10:00:00',
      fechaHoraFin: '2026-07-11T10:30:00',
      estado: 'programada',
      estadoPago: 'pendiente',
      origen: 'interno'
    });
  });
});
