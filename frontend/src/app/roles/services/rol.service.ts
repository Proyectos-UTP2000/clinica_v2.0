import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiResponse } from '../../shared/models/api-response.model';
import { Page } from '../../shared/models/page.model';
import { PermisoResponse, RolCreateRequest, RolResponse, RolUpdateRequest } from '../../shared/models/rol.model';

const API_URL = 'http://localhost:8080/api';

@Injectable()
export class RolService {
  constructor(private readonly http: HttpClient) {}

  listar(page = 0, size = 10): Observable<Page<RolResponse>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<Page<RolResponse>>(`${API_URL}/roles`, { params });
  }

  obtener(id: number): Observable<RolResponse> {
    return this.http.get<RolResponse>(`${API_URL}/roles/${id}`);
  }

  crear(request: RolCreateRequest): Observable<RolResponse> {
    return this.http.post<RolResponse>(`${API_URL}/roles`, request);
  }

  actualizar(id: number, request: RolUpdateRequest): Observable<RolResponse> {
    return this.http.put<RolResponse>(`${API_URL}/roles/${id}`, request);
  }

  asignarPermisos(id: number, permisosIds: number[]): Observable<RolResponse> {
    return this.http.put<RolResponse>(`${API_URL}/roles/${id}/permisos`, { permisosIds });
  }

  desactivar(id: number): Observable<ApiResponse> {
    return this.http.delete<ApiResponse>(`${API_URL}/roles/${id}`);
  }

  listarPermisos(): Observable<PermisoResponse[]> {
    return this.http.get<PermisoResponse[]>(`${API_URL}/permisos`);
  }
}
