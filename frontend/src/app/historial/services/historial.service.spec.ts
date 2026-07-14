import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { HistorialService } from './historial.service';

const API_URL = '/api';

describe('HistorialService', () => {
  let service: HistorialService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [HistorialService, provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(HistorialService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('sube un adjunto usando multipart/form-data', () => {
    const archivo = new File(['PDF'], 'resultado.pdf', { type: 'application/pdf' });

    service.subirAdjunto(9, archivo).subscribe((adjunto) => {
      expect(adjunto.id).toBe(3);
      expect(adjunto.nombreArchivo).toBe('resultado.pdf');
    });

    const req = http.expectOne(`${API_URL}/consultas/9/adjuntos`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body instanceof FormData).toBeTrue();
    req.flush({
      id: 3,
      nombreArchivo: 'resultado.pdf',
      tipoMime: 'application/pdf',
      fechaSubida: '2026-06-22T22:00:00',
      ruta: 'archivo.pdf'
    });
  });

  it('descarga un adjunto como blob', () => {
    service.descargarAdjunto(3).subscribe((blob) => {
      expect(blob.size).toBe(3);
    });

    const req = http.expectOne(`${API_URL}/adjuntos/3`);
    expect(req.request.method).toBe('GET');
    expect(req.request.responseType).toBe('blob');
    req.flush(new Blob(['PDF'], { type: 'application/pdf' }));
  });
});
