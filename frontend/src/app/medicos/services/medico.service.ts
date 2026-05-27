import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { ApiResponse } from '../../shared/models/api-response.model';
import { EspecialidadResponse } from '../../shared/models/especialidad.model';
import { MedicoCreateRequest, MedicoResponse, MedicoUpdateRequest } from '../../shared/models/medico.model';
import { Page } from '../../shared/models/page.model';
import { SedeResponse } from '../../shared/models/sede.model';

const API_URL = 'http://localhost:8080/api';

@Injectable()
export class MedicoService {
  constructor(private readonly http: HttpClient) {}

  listar(page = 0, size = 10, filtros?: { texto?: string; especialidadId?: number; sedeId?: number }): Observable<Page<MedicoResponse>> {
    let params = new HttpParams()
      .set('page', page)
      .set('size', size);

    if (filtros?.texto) {
      params = params.set('texto', filtros.texto);
    }
    if (filtros?.especialidadId) {
      params = params.set('especialidadId', filtros.especialidadId);
    }
    if (filtros?.sedeId) {
      params = params.set('sedeId', filtros.sedeId);
    }

    return this.http.get<Page<MedicoResponse>>(`${API_URL}/medicos`, { params });
  }

  obtenerPorId(id: number): Observable<MedicoResponse> {
    return this.http.get<MedicoResponse>(`${API_URL}/medicos/${id}`);
  }

  crear(request: MedicoCreateRequest): Observable<MedicoResponse> {
    return this.http.post<MedicoResponse>(`${API_URL}/medicos`, request);
  }

  actualizar(id: number, request: MedicoUpdateRequest): Observable<MedicoResponse> {
    return this.http.put<MedicoResponse>(`${API_URL}/medicos/${id}`, request);
  }

  desactivar(id: number): Observable<ApiResponse> {
    return this.http.delete<ApiResponse>(`${API_URL}/medicos/${id}`);
  }

  getEspecialidades(): Observable<EspecialidadResponse[]> {
    return this.http.get<EspecialidadResponse[]>(`${API_URL}/especialidades/todas`);
  }

  getSedes(): Observable<SedeResponse[]> {
    const params = new HttpParams().set('page', 0).set('size', 100);
    return this.http.get<Page<SedeResponse>>(`${API_URL}/sedes`, { params }).pipe(
      map((response) => response.content)
    );
  }
}
