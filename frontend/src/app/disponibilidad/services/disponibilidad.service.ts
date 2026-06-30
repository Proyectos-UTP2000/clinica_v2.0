import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { ApiResponse } from '../../shared/models/api-response.model';
import {
  DisponibilidadBaseCreateRequest,
  DisponibilidadBaseResponse,
  ExcepcionDisponibilidadCreateRequest,
  ExcepcionDisponibilidadResponse
} from '../../shared/models/disponibilidad.model';
import { MedicoResponse } from '../../shared/models/medico.model';
import { Page } from '../../shared/models/page.model';
import { SedeResponse } from '../../shared/models/sede.model';

const API_URL = 'http://localhost:8080/api';

@Injectable()
export class DisponibilidadService {
  constructor(private readonly http: HttpClient) {}

  listarMedicos(sedeId?: number): Observable<MedicoResponse[]> {
    let params = new HttpParams().set('page', 0).set('size', 100);
    if (sedeId) {
      params = params.set('sedeId', sedeId);
    }
    return this.http.get<Page<MedicoResponse>>(`${API_URL}/medicos`, { params }).pipe(map((response) => response.content));
  }

  obtenerMedicoAutenticado(): Observable<MedicoResponse> {
    return this.http.get<MedicoResponse>(`${API_URL}/medicos/me`);
  }

  listarSedes(): Observable<SedeResponse[]> {
    const params = new HttpParams().set('page', 0).set('size', 100);
    return this.http.get<Page<SedeResponse>>(`${API_URL}/sedes`, { params }).pipe(map((response) => response.content));
  }

  listarBases(doctorId: number): Observable<DisponibilidadBaseResponse[]> {
    return this.http.get<DisponibilidadBaseResponse[]>(`${API_URL}/disponibilidad/doctor/${doctorId}/base`);
  }

  guardarBase(doctorId: number, request: DisponibilidadBaseCreateRequest, forzar = false): Observable<DisponibilidadBaseResponse> {
    const params = new HttpParams().set('forzar', forzar.toString());
    return this.http.post<DisponibilidadBaseResponse>(`${API_URL}/disponibilidad/doctor/${doctorId}/base`, request, { params });
  }

  eliminarBase(doctorId: number, id: number): Observable<ApiResponse> {
    return this.http.delete<ApiResponse>(`${API_URL}/disponibilidad/doctor/${doctorId}/base/${id}`);
  }

  listarExcepciones(doctorId: number, fechaInicio?: string, fechaFin?: string): Observable<ExcepcionDisponibilidadResponse[]> {
    let params = new HttpParams();
    if (fechaInicio) {
      params = params.set('fechaInicio', fechaInicio);
    }
    if (fechaFin) {
      params = params.set('fechaFin', fechaFin);
    }
    return this.http.get<ExcepcionDisponibilidadResponse[]>(`${API_URL}/disponibilidad/doctor/${doctorId}/excepciones`, { params });
  }

  crearExcepcion(doctorId: number, request: ExcepcionDisponibilidadCreateRequest): Observable<ExcepcionDisponibilidadResponse> {
    return this.http.post<ExcepcionDisponibilidadResponse>(`${API_URL}/disponibilidad/doctor/${doctorId}/excepciones`, request);
  }

  eliminarExcepcion(doctorId: number, id: number): Observable<ApiResponse> {
    return this.http.delete<ApiResponse>(`${API_URL}/disponibilidad/doctor/${doctorId}/excepciones/${id}`);
  }
}
