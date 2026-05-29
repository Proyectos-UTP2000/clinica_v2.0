import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CitaResponse } from '../../shared/models/cita.model';
import { EspecialidadResponse } from '../../shared/models/especialidad.model';
import { MedicoResponse } from '../../shared/models/medico.model';
import { Page } from '../../shared/models/page.model';
import { SedeResponse } from '../../shared/models/sede.model';
import { CitaService } from '../../citas/services/cita.service';

const API_URL = 'http://localhost:8080/api';

@Injectable()
export class AgendaService {
  constructor(
    private readonly http: HttpClient,
    private readonly citaService: CitaService
  ) {}

  listarCitas(filtros: { fechaInicio: string; fechaFin: string; doctorId?: number; sedeId?: number }, propias = false, page = 0, size = 200): Observable<Page<CitaResponse>> {
    if (propias) {
      return this.citaService.listarPropiasRango({ fechaInicio: filtros.fechaInicio, fechaFin: filtros.fechaFin }, page, size);
    }
    return this.citaService.listar(filtros, page, size);
  }

  listarSedes(): Observable<SedeResponse[]> {
    return this.citaService.listarSedes();
  }

  listarMedicos(filtros?: { especialidadId?: number; sedeId?: number }): Observable<MedicoResponse[]> {
    return this.citaService.listarMedicos(filtros);
  }

  listarEspecialidades(): Observable<EspecialidadResponse[]> {
    return this.http.get<EspecialidadResponse[]>(`${API_URL}/especialidades/todas`);
  }
}
