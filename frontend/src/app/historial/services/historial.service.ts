import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { AdjuntoResponse, ConsultaCreateRequest, ConsultaResponse } from '../../shared/models/consulta.model';
import { MedicoResponse } from '../../shared/models/medico.model';
import { PacienteResponse } from '../../shared/models/paciente.model';
import { Page } from '../../shared/models/page.model';
import { SedeResponse } from '../../shared/models/sede.model';

const API_URL = 'http://localhost:8080/api';

@Injectable()
export class HistorialService {
  constructor(private readonly http: HttpClient) {}

  listarPacientes(): Observable<PacienteResponse[]> {
    const params = new HttpParams().set('page', 0).set('size', 100);
    return this.http.get<Page<PacienteResponse>>(`${API_URL}/pacientes`, { params }).pipe(map((response) => response.content));
  }

  listarMedicos(): Observable<MedicoResponse[]> {
    const params = new HttpParams().set('page', 0).set('size', 100);
    return this.http.get<Page<MedicoResponse>>(`${API_URL}/medicos`, { params }).pipe(map((response) => response.content));
  }

  listarSedes(): Observable<SedeResponse[]> {
    const params = new HttpParams().set('page', 0).set('size', 100);
    return this.http.get<Page<SedeResponse>>(`${API_URL}/sedes`, { params }).pipe(map((response) => response.content));
  }

  listarPorPaciente(pacienteId: number, page = 0, size = 10): Observable<Page<ConsultaResponse>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<Page<ConsultaResponse>>(`${API_URL}/consultas/paciente/${pacienteId}`, { params });
  }

  listarPorDoctor(page = 0, size = 10): Observable<Page<ConsultaResponse>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<Page<ConsultaResponse>>(`${API_URL}/consultas/doctor`, { params });
  }

  obtener(id: number): Observable<ConsultaResponse> {
    return this.http.get<ConsultaResponse>(`${API_URL}/consultas/${id}`);
  }

  crear(request: ConsultaCreateRequest): Observable<ConsultaResponse> {
    return this.http.post<ConsultaResponse>(`${API_URL}/consultas`, request);
  }

  agregarNota(consultaId: number, nota: string): Observable<ConsultaResponse> {
    return this.http.post<ConsultaResponse>(`${API_URL}/consultas/${consultaId}/notas`, { nota });
  }

  subirAdjunto(consultaId: number, archivo: File): Observable<AdjuntoResponse> {
    const formData = new FormData();
    formData.append('archivo', archivo);
    return this.http.post<AdjuntoResponse>(`${API_URL}/consultas/${consultaId}/adjuntos`, formData);
  }

  descargarAdjunto(adjuntoId: number): Observable<Blob> {
    return this.http.get(`${API_URL}/adjuntos/${adjuntoId}`, { responseType: 'blob' });
  }

  buscarPacientePorDni(dni: string): Observable<PacienteResponse> {
    const params = new HttpParams().set('dni', dni);
    return this.http.get<PacienteResponse>(`${API_URL}/pacientes/buscar`, { params });
  }

  obtenerMedicoAutenticado(): Observable<MedicoResponse> {
    return this.http.get<MedicoResponse>(`${API_URL}/medicos/me`);
  }

  obtenerPacientePorId(id: number): Observable<PacienteResponse> {
    return this.http.get<PacienteResponse>(`${API_URL}/pacientes/${id}`);
  }

  listarCitasDoctor(): Observable<any> {
    const params = new HttpParams().set('page', 0).set('size', 100);
    return this.http.get<any>(`${API_URL}/citas/doctor`, { params });
  }
}
