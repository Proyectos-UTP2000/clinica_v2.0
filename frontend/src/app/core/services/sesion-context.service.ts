import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, map, Observable } from 'rxjs';
import { SedeResponse } from '../../shared/models/sede.model';
import { Page } from '../../shared/models/page.model';

const API_URL = 'http://localhost:8080/api';

@Injectable({
  providedIn: 'root'
})
export class SesionContextService {
  private readonly selectedSedeIdSubject = new BehaviorSubject<number | null>(null);
  readonly selectedSedeId$ = this.selectedSedeIdSubject.asObservable();

  private readonly sedesSubject = new BehaviorSubject<SedeResponse[]>([]);
  readonly sedes$ = this.sedesSubject.asObservable();

  constructor(private readonly http: HttpClient) {
    this.cargarSedes();
  }

  get selectedSedeId(): number | null {
    return this.selectedSedeIdSubject.value;
  }

  setSede(sedeId: number | null): void {
    this.selectedSedeIdSubject.next(sedeId);
  }

  private cargarSedes(): void {
    const params = new HttpParams().set('page', 0).set('size', 100);
    this.http.get<Page<SedeResponse>>(`${API_URL}/sedes`, { params })
      .pipe(map((res) => res.content))
      .subscribe({
        next: (sedes) => this.sedesSubject.next(sedes),
        error: () => console.error('No se pudieron cargar las sedes para el contexto global.')
      });
  }
}
