import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { DashboardTotalesResponse } from '../../shared/models/dashboard-totales.model';

const API_URL = '/api';

@Injectable({
  providedIn: 'root'
})
export class DashboardService {
  constructor(private readonly http: HttpClient) {}

  obtenerTotales(): Observable<DashboardTotalesResponse> {
    return this.http.get<DashboardTotalesResponse>(`${API_URL}/dashboard/totales`);
  }
}
