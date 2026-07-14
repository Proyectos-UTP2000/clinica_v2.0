import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { AdjuntoResponse, ConsultaCreateRequest, ConsultaResponse, EstudioComplementarioResponse } from '../../shared/models/consulta.model';
import { MedicoResponse } from '../../shared/models/medico.model';
import { PacienteResponse } from '../../shared/models/paciente.model';
import { Page } from '../../shared/models/page.model';
import { SedeResponse } from '../../shared/models/sede.model';


const API_URL = '/api';

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

  listarPorPaciente(pacienteId: number, page = 0, size = 10, search = '', tieneRecetas = false, tieneEstudios = false, tieneAdjuntos = false, fechaInicio = '', fechaFin = ''): Observable<Page<ConsultaResponse>> {
    let params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sort', 'fechaHora,desc');
    
    if (search) {
      params = params.set('search', search);
    }
    if (tieneRecetas) {
      params = params.set('tieneRecetas', 'true');
    }
    if (tieneEstudios) {
      params = params.set('tieneEstudios', 'true');
    }
    if (tieneAdjuntos) {
      params = params.set('tieneAdjuntos', 'true');
    }
    if (fechaInicio) {
      params = params.set('fechaInicio', fechaInicio);
    }
    if (fechaFin) {
      params = params.set('fechaFin', fechaFin);
    }

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

  descargarPdf(consultaId: number): Observable<Blob> {
    return this.http.get(`${API_URL}/consultas/${consultaId}/pdf`, { responseType: 'blob' });
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

  listarEstudios(estado?: string, filtro?: string, page = 0, size = 10): Observable<Page<EstudioComplementarioResponse>> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (estado) {
      params = params.set('estado', estado);
    }
    if (filtro) {
      params = params.set('filtro', filtro);
    }
    return this.http.get<Page<EstudioComplementarioResponse>>(`${API_URL}/estudios`, { params });
  }

  subirResultadoEstudio(estudioId: number, archivos: File[]): Observable<EstudioComplementarioResponse> {
    const formData = new FormData();
    archivos.forEach(archivo => {
      formData.append('archivos', archivo);
    });
    return this.http.post<EstudioComplementarioResponse>(`${API_URL}/estudios/${estudioId}/resultado`, formData);
  }

  descargarResultadoEstudio(estudioId: number, index = 0): Observable<Blob> {
    const params = new HttpParams().set('index', index);
    return this.http.get(`${API_URL}/estudios/${estudioId}/resultado/descargar`, { params, responseType: 'blob' });
  }
}

