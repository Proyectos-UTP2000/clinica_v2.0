import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { CitaResponse } from '../../shared/models/cita.model';
import { PacienteResponse } from '../../shared/models/paciente.model';
import { Page } from '../../shared/models/page.model';
import { PagoCreateRequest, PagoResponse } from '../../shared/models/pago.model';

const API_URL = 'http://localhost:8080/api';

@Injectable()
export class PagoService {
  constructor(private readonly http: HttpClient) {}

  listarPacientes(): Observable<PacienteResponse[]> {
    const params = new HttpParams().set('page', 0).set('size', 100);
    return this.http.get<Page<PacienteResponse>>(`${API_URL}/pacientes`, { params }).pipe(map((response) => response.content));
  }

  listarCitas(): Observable<CitaResponse[]> {
    const params = new HttpParams().set('page', 0).set('size', 100);
    return this.http.get<Page<CitaResponse>>(`${API_URL}/citas`, { params }).pipe(map((response) => response.content));
  }

  obtenerPorCita(citaId: number): Observable<PagoResponse> {
    return this.http.get<PagoResponse>(`${API_URL}/pagos/cita/${citaId}`);
  }

  obtenerCita(citaId: number): Observable<CitaResponse> {
    return this.http.get<CitaResponse>(`${API_URL}/citas/${citaId}`);
  }

  listarPorPaciente(pacienteId: number): Observable<PagoResponse[]> {
    return this.http.get<PagoResponse[]>(`${API_URL}/pagos/paciente/${pacienteId}`);
  }

  registrar(request: PagoCreateRequest): Observable<PagoResponse> {
    return this.http.post<PagoResponse>(`${API_URL}/pagos`, request);
  }
}
