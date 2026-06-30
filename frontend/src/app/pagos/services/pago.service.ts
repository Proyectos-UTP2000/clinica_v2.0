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

  listarPorCaja(cajaId: number): Observable<PagoResponse[]> {
    return this.http.get<PagoResponse[]>(`${API_URL}/pagos/caja/${cajaId}`);
  }

  registrar(request: PagoCreateRequest): Observable<PagoResponse> {
    return this.http.post<PagoResponse>(`${API_URL}/pagos`, request);
  }

  abrirCaja(montoApertura: number, observaciones?: string): Observable<any> {
    return this.http.post<any>(`${API_URL}/caja/abrir`, { montoApertura, observaciones });
  }

  cerrarCaja(balanceReal: number, observaciones?: string): Observable<any> {
    return this.http.post<any>(`${API_URL}/caja/cerrar`, { balanceReal, observaciones });
  }

  obtenerCajaHoy(): Observable<any> {
    return this.http.get<any>(`${API_URL}/caja/hoy`);
  }

  descargarReportePdf(cajaId: number): Observable<Blob> {
    return this.http.get(`${API_URL}/caja/${cajaId}/reporte-pdf`, { responseType: 'blob' });
  }

  reabrirCaja(): Observable<any> {
    return this.http.post<any>(`${API_URL}/caja/reabrir`, {});
  }
}
