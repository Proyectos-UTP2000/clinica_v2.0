import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { RolService } from './rol.service';

const API_URL = 'http://localhost:8080/api';

describe('RolService', () => {
  let service: RolService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [RolService, provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(RolService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('lista roles paginados desde /api/roles', () => {
    service.listar(2, 5).subscribe((response) => {
      expect(response.content.length).toBe(1);
      expect(response.content[0].nombre).toBe('Administrador');
    });

    const req = http.expectOne(`${API_URL}/roles?page=2&size=5`);
    expect(req.request.method).toBe('GET');
    req.flush({ content: [{ id: 1, nombre: 'Administrador', activo: true, permisos: [] }], totalElements: 1, totalPages: 1, size: 5, number: 2 });
  });

  it('obtiene permisos y guarda checklist de rol', () => {
    service.listarPermisos().subscribe((permisos) => {
      expect(permisos[0].codigo).toBe('roles.ver');
    });
    const permisosReq = http.expectOne(`${API_URL}/permisos`);
    expect(permisosReq.request.method).toBe('GET');
    permisosReq.flush([{ id: 1, codigo: 'roles.ver', descripcion: 'Ver roles' }]);

    service.asignarPermisos(7, [1, 2]).subscribe((rol) => {
      expect(rol.id).toBe(7);
      expect(rol.permisos.length).toBe(2);
    });
    const asignarReq = http.expectOne(`${API_URL}/roles/7/permisos`);
    expect(asignarReq.request.method).toBe('PUT');
    expect(asignarReq.request.body).toEqual({ permisosIds: [1, 2] });
    asignarReq.flush({ id: 7, nombre: 'Secretaria', activo: true, permisos: [{ id: 1 }, { id: 2 }] });
  });
});
