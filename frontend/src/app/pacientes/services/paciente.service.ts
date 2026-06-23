import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiResponse } from '../../shared/models/api-response.model';
import { DniInfo } from '../../shared/models/dni-info.model';
import { PacienteCreateRequest, PacienteResponse, PacienteUpdateRequest } from '../../shared/models/paciente.model';
import { Page } from '../../shared/models/page.model';

const API_URL = 'http://localhost:8080/api';

@Injectable()
export class PacienteService {
  constructor(private readonly http: HttpClient) {}

  listar(page = 0, size = 10): Observable<Page<PacienteResponse>> {
    const params = new HttpParams()
      .set('page', page)
      .set('size', size);
    return this.http.get<Page<PacienteResponse>>(`${API_URL}/pacientes`, { params });
  }

  obtenerPorId(id: number): Observable<PacienteResponse> {
    return this.http.get<PacienteResponse>(`${API_URL}/pacientes/${id}`);
  }

  crear(request: PacienteCreateRequest): Observable<PacienteResponse> {
    return this.http.post<PacienteResponse>(`${API_URL}/pacientes`, request);
  }

  buscarPorDni(dni: string): Observable<PacienteResponse> {
    const params = new HttpParams().set('dni', dni);
    return this.http.get<PacienteResponse>(`${API_URL}/pacientes/buscar`, { params });
  }

  consultarDni(dni: string): Observable<DniInfo> {
    const params = new HttpParams().set('dni', dni);
    return this.http.get<DniInfo>(`${API_URL}/pacientes/buscar-dni`, { params });
  }

  actualizar(id: number, request: PacienteUpdateRequest): Observable<PacienteResponse> {
    return this.http.put<PacienteResponse>(`${API_URL}/pacientes/${id}`, request);
  }

  desactivar(id: number): Observable<ApiResponse> {
    return this.http.delete<ApiResponse>(`${API_URL}/pacientes/${id}`);
  }
}
