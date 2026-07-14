import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiResponse } from '../../shared/models/api-response.model';
import { EspecialidadCreateRequest, EspecialidadResponse, EspecialidadUpdateRequest } from '../../shared/models/especialidad.model';
import { Page } from '../../shared/models/page.model';

const API_URL = '/api';

@Injectable()
export class EspecialidadService {
  constructor(private readonly http: HttpClient) {}

  listar(page = 0, size = 10): Observable<Page<EspecialidadResponse>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<Page<EspecialidadResponse>>(`${API_URL}/especialidades`, { params });
  }

  listarTodas(): Observable<EspecialidadResponse[]> {
    return this.http.get<EspecialidadResponse[]>(`${API_URL}/especialidades/todas`);
  }

  obtener(id: number): Observable<EspecialidadResponse> {
    return this.http.get<EspecialidadResponse>(`${API_URL}/especialidades/${id}`);
  }

  crear(request: EspecialidadCreateRequest): Observable<EspecialidadResponse> {
    return this.http.post<EspecialidadResponse>(`${API_URL}/especialidades`, request);
  }

  actualizar(id: number, request: EspecialidadUpdateRequest): Observable<EspecialidadResponse> {
    return this.http.put<EspecialidadResponse>(`${API_URL}/especialidades/${id}`, request);
  }

  eliminar(id: number): Observable<ApiResponse> {
    return this.http.delete<ApiResponse>(`${API_URL}/especialidades/${id}`);
  }
}
