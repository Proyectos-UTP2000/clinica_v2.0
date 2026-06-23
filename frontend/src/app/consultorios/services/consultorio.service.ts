import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiResponse } from '../../shared/models/api-response.model';
import { Page } from '../../shared/models/page.model';
import { ConsultorioCreateRequest, ConsultorioResponse, ConsultorioUpdateRequest } from '../../shared/models/consultorio.model';

const API_URL = 'http://localhost:8080/api';

@Injectable({
  providedIn: 'root'
})
export class ConsultorioService {
  constructor(private readonly http: HttpClient) {}

  listar(page = 0, size = 10): Observable<Page<ConsultorioResponse>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<Page<ConsultorioResponse>>(`${API_URL}/consultorios`, { params });
  }

  obtener(id: number): Observable<ConsultorioResponse> {
    return this.http.get<ConsultorioResponse>(`${API_URL}/consultorios/${id}`);
  }

  listarPorSede(sedeId: number): Observable<ConsultorioResponse[]> {
    return this.http.get<ConsultorioResponse[]>(`${API_URL}/consultorios/sede/${sedeId}`);
  }

  crear(request: ConsultorioCreateRequest): Observable<ConsultorioResponse> {
    return this.http.post<ConsultorioResponse>(`${API_URL}/consultorios`, request);
  }

  actualizar(id: number, request: ConsultorioUpdateRequest): Observable<ConsultorioResponse> {
    return this.http.put<ConsultorioResponse>(`${API_URL}/consultorios/${id}`, request);
  }

  desactivar(id: number): Observable<ApiResponse> {
    return this.http.delete<ApiResponse>(`${API_URL}/consultorios/${id}`);
  }
}
