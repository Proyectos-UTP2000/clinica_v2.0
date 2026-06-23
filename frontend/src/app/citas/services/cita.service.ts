import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { ApiResponse } from '../../shared/models/api-response.model';
import { CitaCreateRequest, CitaResponse, CitaUpdateRequest, DisponibilidadSlotResponse } from '../../shared/models/cita.model';
import { MedicoResponse } from '../../shared/models/medico.model';
import { PacienteResponse } from '../../shared/models/paciente.model';
import { Page } from '../../shared/models/page.model';
import { SedeResponse } from '../../shared/models/sede.model';

const API_URL = 'http://localhost:8080/api';

@Injectable()
export class CitaService {
  constructor(private readonly http: HttpClient) {}

  listar(filtros?: {
    pacienteId?: number;
    doctorId?: number;
    sedeId?: number;
    fecha?: string;
    fechaInicio?: string;
    fechaFin?: string;
  }, page = 0, size = 10): Observable<Page<CitaResponse>> {
    let params = new HttpParams()
      .set('page', page)
      .set('size', size);

    if (filtros?.pacienteId) {
      params = params.set('pacienteId', filtros.pacienteId);
    }
    if (filtros?.doctorId) {
      params = params.set('doctorId', filtros.doctorId);
    }
    if (filtros?.sedeId) {
      params = params.set('sedeId', filtros.sedeId);
    }
    if (filtros?.fecha) {
      params = params.set('fecha', filtros.fecha);
    }
    if (filtros?.fechaInicio) {
      params = params.set('fechaInicio', filtros.fechaInicio);
    }
    if (filtros?.fechaFin) {
      params = params.set('fechaFin', filtros.fechaFin);
    }

    return this.http.get<Page<CitaResponse>>(`${API_URL}/citas`, { params });
  }

  listarPropias(fecha?: string, page = 0, size = 10): Observable<Page<CitaResponse>> {
    let params = new HttpParams()
      .set('page', page)
      .set('size', size);

    if (fecha) {
      params = params.set('fecha', fecha);
    }

    return this.http.get<Page<CitaResponse>>(`${API_URL}/citas/doctor`, { params });
  }

  listarPropiasRango(filtros: { fechaInicio?: string; fechaFin?: string; fecha?: string }, page = 0, size = 10): Observable<Page<CitaResponse>> {
    let params = new HttpParams()
      .set('page', page)
      .set('size', size);
    if (filtros.fecha) {
      params = params.set('fecha', filtros.fecha);
    }
    if (filtros.fechaInicio) {
      params = params.set('fechaInicio', filtros.fechaInicio);
    }
    if (filtros.fechaFin) {
      params = params.set('fechaFin', filtros.fechaFin);
    }
    return this.http.get<Page<CitaResponse>>(`${API_URL}/citas/doctor`, { params });
  }

  obtenerPorId(id: number): Observable<CitaResponse> {
    return this.http.get<CitaResponse>(`${API_URL}/citas/${id}`);
  }

  crear(request: CitaCreateRequest): Observable<CitaResponse> {
    return this.http.post<CitaResponse>(`${API_URL}/citas`, request);
  }

  reprogramar(id: number, nuevaFechaHora: string, doctorId?: number): Observable<CitaResponse> {
    const request: CitaUpdateRequest = doctorId ? { nuevaFechaHora, doctorId } : { nuevaFechaHora };
    return this.http.put<CitaResponse>(`${API_URL}/citas/${id}/reprogramar`, request);
  }

  cancelar(id: number): Observable<ApiResponse> {
    return this.http.put<ApiResponse>(`${API_URL}/citas/${id}/cancelar`, {});
  }

  obtenerSlotsDisponibles(doctorId: number, sedeId: number, fecha: string): Observable<DisponibilidadSlotResponse[]> {
    const params = new HttpParams()
      .set('doctorId', doctorId)
      .set('sedeId', sedeId)
      .set('fecha', fecha);
    return this.http.get<DisponibilidadSlotResponse[]>(`${API_URL}/citas/slots-disponibles`, { params });
  }

  listarPacientes(): Observable<PacienteResponse[]> {
    const params = new HttpParams().set('page', 0).set('size', 100);
    return this.http.get<Page<PacienteResponse>>(`${API_URL}/pacientes`, { params }).pipe(map((response) => response.content));
  }

  listarMedicos(filtros?: { especialidadId?: number; sedeId?: number }): Observable<MedicoResponse[]> {
    let params = new HttpParams().set('page', 0).set('size', 100);
    if (filtros?.especialidadId) {
      params = params.set('especialidadId', filtros.especialidadId);
    }
    if (filtros?.sedeId) {
      params = params.set('sedeId', filtros.sedeId);
    }
    return this.http.get<Page<MedicoResponse>>(`${API_URL}/medicos/buscar`, { params }).pipe(map((response) => response.content));
  }

  listarSedes(): Observable<SedeResponse[]> {
    const params = new HttpParams().set('page', 0).set('size', 100);
    return this.http.get<Page<SedeResponse>>(`${API_URL}/sedes`, { params }).pipe(map((response) => response.content));
  }

  listarDisponibilidadBase(doctorId: number): Observable<any[]> {
    return this.http.get<any[]>(`${API_URL}/disponibilidad/doctor/${doctorId}/base`);
  }
}
