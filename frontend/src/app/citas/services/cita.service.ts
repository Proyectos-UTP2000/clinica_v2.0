import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { ApiResponse } from '../../shared/models/api-response.model';
import { CitaCreateRequest, CitaResponse, DisponibilidadSlotResponse } from '../../shared/models/cita.model';
import { MedicoResponse } from '../../shared/models/medico.model';
import { PacienteResponse } from '../../shared/models/paciente.model';
import { Page } from '../../shared/models/page.model';
import { SedeResponse } from '../../shared/models/sede.model';

const API_URL = 'http://localhost:8080/api';

@Injectable()
export class CitaService {
  constructor(private readonly http: HttpClient) {}

  listar(filtros?: { pacienteId?: number; doctorId?: number; fecha?: string }, page = 0, size = 10): Observable<Page<CitaResponse>> {
    let params = new HttpParams()
      .set('page', page)
      .set('size', size);

    if (filtros?.pacienteId) {
      params = params.set('pacienteId', filtros.pacienteId);
    }
    if (filtros?.doctorId) {
      params = params.set('doctorId', filtros.doctorId);
    }
    if (filtros?.fecha) {
      params = params.set('fecha', filtros.fecha);
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

  obtenerPorId(id: number): Observable<CitaResponse> {
    return this.http.get<CitaResponse>(`${API_URL}/citas/${id}`);
  }

  crear(request: CitaCreateRequest): Observable<CitaResponse> {
    return this.http.post<CitaResponse>(`${API_URL}/citas`, request);
  }

  reprogramar(id: number, nuevaFechaHora: string): Observable<CitaResponse> {
    return this.http.put<CitaResponse>(`${API_URL}/citas/${id}/reprogramar`, { nuevaFechaHora });
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
    return this.http.get<Page<MedicoResponse>>(`${API_URL}/medicos`, { params }).pipe(map((response) => response.content));
  }

  listarSedes(): Observable<SedeResponse[]> {
    const params = new HttpParams().set('page', 0).set('size', 100);
    return this.http.get<Page<SedeResponse>>(`${API_URL}/sedes`, { params }).pipe(map((response) => response.content));
  }
}
