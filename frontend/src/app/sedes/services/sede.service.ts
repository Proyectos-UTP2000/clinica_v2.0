import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { ApiResponse } from '../../shared/models/api-response.model';
import { Page } from '../../shared/models/page.model';
import { SedeCreateRequest, SedeResponse, SedeUpdateRequest } from '../../shared/models/sede.model';

const API_URL = 'http://localhost:8080/api';

@Injectable({
  providedIn: 'root'
})
export class SedeService {
  constructor(private readonly http: HttpClient) {}

  listarTodas(): Observable<SedeResponse[]> {
    const params = new HttpParams().set('page', 0).set('size', 100);
    return this.http.get<Page<SedeResponse>>(`${API_URL}/sedes`, { params }).pipe(
      map((response) => response.content)
    );
  }

  listar(page = 0, size = 10): Observable<Page<SedeResponse>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<Page<SedeResponse>>(`${API_URL}/sedes`, { params });
  }

  obtener(id: number): Observable<SedeResponse> {
    return this.http.get<SedeResponse>(`${API_URL}/sedes/${id}`);
  }

  crear(request: SedeCreateRequest): Observable<SedeResponse> {
    return this.http.post<SedeResponse>(`${API_URL}/sedes`, request);
  }

  actualizar(id: number, request: SedeUpdateRequest): Observable<SedeResponse> {
    return this.http.put<SedeResponse>(`${API_URL}/sedes/${id}`, request);
  }

  desactivar(id: number): Observable<ApiResponse> {
    return this.http.delete<ApiResponse>(`${API_URL}/sedes/${id}`);
  }

  obtenerConfiguracionGlobal(): Observable<any[]> {
    return this.http.get<any[]>(`${API_URL}/config`);
  }

  actualizarConfiguracionGlobal(clave: string, valor: string): Observable<any> {
    return this.http.put<any>(`${API_URL}/config/${clave}`, { valor });
  }
}
