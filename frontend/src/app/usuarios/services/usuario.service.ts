import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { ApiResponse } from '../../shared/models/api-response.model';
import { DniInfo } from '../../shared/models/dni-info.model';
import { Page } from '../../shared/models/page.model';
import { RolResponse } from '../../shared/models/rol.model';
import { MedicoResponse } from '../../shared/models/medico.model';
import { UsuarioResponse, UsuarioCreateRequest, UsuarioUpdateRequest } from '../../shared/models/usuario.model';

const API_URL = '/api';

@Injectable()
export class UsuarioService {
  constructor(private readonly http: HttpClient) {}

  listar(page = 0, size = 10): Observable<Page<UsuarioResponse>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<Page<UsuarioResponse>>(`${API_URL}/usuarios`, { params });
  }

  obtener(id: number): Observable<UsuarioResponse> {
    return this.http.get<UsuarioResponse>(`${API_URL}/usuarios/${id}`);
  }

  crear(request: UsuarioCreateRequest): Observable<UsuarioResponse> {
    return this.http.post<UsuarioResponse>(`${API_URL}/usuarios`, request);
  }

  actualizar(id: number, request: UsuarioUpdateRequest): Observable<UsuarioResponse> {
    return this.http.put<UsuarioResponse>(`${API_URL}/usuarios/${id}`, request);
  }

  desactivar(id: number): Observable<ApiResponse> {
    return this.http.delete<ApiResponse>(`${API_URL}/usuarios/${id}`);
  }

  consultarDni(dni: string): Observable<DniInfo> {
    const params = new HttpParams().set('dni', dni);
    return this.http.get<DniInfo>(`${API_URL}/pacientes/buscar-dni`, { params });
  }

  listarRoles(): Observable<RolResponse[]> {
    const params = new HttpParams().set('page', 0).set('size', 100);
    return this.http.get<Page<RolResponse>>(`${API_URL}/roles`, { params }).pipe(
      map((response) => response.content)
    );
  }

  listarMedicos(): Observable<MedicoResponse[]> {
    const params = new HttpParams().set('page', 0).set('size', 200);
    return this.http.get<Page<MedicoResponse>>(`${API_URL}/medicos`, { params }).pipe(
      map((response) => response.content)
    );
  }

  listarAuditoria(page = 0, size = 20): Observable<Page<any>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<Page<any>>(`${API_URL}/audit-logs`, { params });
  }
}
