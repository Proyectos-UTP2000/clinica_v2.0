import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, map, Observable } from 'rxjs';
import { SedeResponse } from '../../shared/models/sede.model';
import { Page } from '../../shared/models/page.model';
import { AuthService } from './auth.service';
import { MedicoResponse } from '../../shared/models/medico.model';

const API_URL = '/api';

@Injectable({
  providedIn: 'root'
})
export class SesionContextService {
  private readonly selectedSedeIdSubject = new BehaviorSubject<number | null>(null);
  readonly selectedSedeId$ = this.selectedSedeIdSubject.asObservable();

  private readonly sedesSubject = new BehaviorSubject<SedeResponse[]>([]);
  readonly sedes$ = this.sedesSubject.asObservable();

  constructor(
    private readonly http: HttpClient,
    private readonly authService: AuthService
  ) {
    this.cargarSedes();
  }

  get selectedSedeId(): number | null {
    return this.selectedSedeIdSubject.value;
  }

  setSede(sedeId: number | null): void {
    this.selectedSedeIdSubject.next(sedeId);
  }

  cargarSedes(): void {
    if (!this.authService.isLoggedIn()) {
      return;
    }

    if (this.authService.hasRole('Doctor')) {
      this.http.get<MedicoResponse>(`${API_URL}/medicos/me`).subscribe({
        next: (medico) => {
          const doctorSedes: SedeResponse[] = (medico.sedesIds || []).map((id, index) => ({
            id,
            nombre: medico.sedes[index] || 'Sede',
            direccion: '',
            activo: true
          }));
          this.sedesSubject.next(doctorSedes);
          if (doctorSedes.length > 0 && !this.selectedSedeId) {
            this.setSede(doctorSedes[0].id);
          }
        },
        error: () => console.error('No se pudieron cargar las sedes del médico.')
      });
    } else {
      const params = new HttpParams().set('page', 0).set('size', 100);
      this.http.get<Page<SedeResponse>>(`${API_URL}/sedes`, { params })
        .pipe(map((res) => res.content))
        .subscribe({
          next: (sedes) => {
            this.sedesSubject.next(sedes);
            if (sedes.length > 0 && !this.authService.hasRole('Administrador') && !this.selectedSedeId) {
              this.setSede(sedes[0].id);
            }
          },
          error: () => console.error('No se pudieron cargar las sedes para el contexto global.')
        });
    }
  }
}
