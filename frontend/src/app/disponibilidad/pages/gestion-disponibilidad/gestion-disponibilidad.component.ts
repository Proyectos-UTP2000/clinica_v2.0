import { Component, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { finalize, forkJoin, map } from 'rxjs';
import { AuthService } from '../../../core/services/auth.service';
import { SesionContextService } from '../../../core/services/sesion-context.service';
import {
  DisponibilidadBaseResponse,
  ExcepcionDisponibilidadResponse
} from '../../../shared/models/disponibilidad.model';
import { MedicoResponse } from '../../../shared/models/medico.model';
import { SedeResponse } from '../../../shared/models/sede.model';
import { DisponibilidadService } from '../../services/disponibilidad.service';

@Component({
    selector: 'app-gestion-disponibilidad',
    templateUrl: './gestion-disponibilidad.component.html',
    standalone: false
})
export class GestionDisponibilidadComponent implements OnInit {
  medicos: MedicoResponse[] = [];
  sedes: SedeResponse[] = [];
  horariosBase: DisponibilidadBaseResponse[] = [];
  excepciones: ExcepcionDisponibilidadResponse[] = [];
  doctorId: number | null = null;
  cargando = false;
  guardandoBase = false;
  guardandoExcepcion = false;
  mensajeError = '';
  mensajeExito = '';
  dias = [
    { id: 1, nombre: 'Lunes' },
    { id: 2, nombre: 'Martes' },
    { id: 3, nombre: 'Miercoles' },
    { id: 4, nombre: 'Jueves' },
    { id: 5, nombre: 'Viernes' },
    { id: 6, nombre: 'Sabado' },
    { id: 7, nombre: 'Domingo' }
  ];

  baseForm = this.fb.group({
    sedeId: [null as number | null, Validators.required],
    diaSemana: [1, Validators.required],
    horaInicio: ['08:00', Validators.required],
    horaFin: ['13:00', Validators.required]
  });

  excepcionForm = this.fb.group({
    fecha: ['', Validators.required],
    horaInicio: ['08:00', Validators.required],
    horaFin: ['09:00', Validators.required],
    motivo: ['', Validators.required]
  });

  filtroExcepcionForm = this.fb.group({
    fechaInicio: [this.hoy()],
    fechaFin: [this.enTreintaDias()]
  });

  constructor(
    public readonly authService: AuthService,
    private readonly fb: FormBuilder,
    private readonly disponibilidadService: DisponibilidadService,
    private readonly sesionContextService: SesionContextService
  ) {}

  ngOnInit(): void {
    this.disponibilidadService.listarSedes().subscribe({
      next: (sedes) => {
        this.sedes = sedes;
        this.sesionContextService.selectedSedeId$.subscribe((sedeId) => {
          this.cargarParaSede(sedeId);
        });
      },
      error: () => (this.mensajeError = 'No se pudieron cargar las sedes.')
    });
  }

  cargarParaSede(sedeId: number | null): void {
    this.cargando = true;
    this.mensajeError = '';
    const activeSede = sedeId || undefined;
    const doctor$ = this.authService.hasPermission('disponibilidad.ver_todas')
      ? this.disponibilidadService.listarMedicos(activeSede)
      : this.disponibilidadService.obtenerMedicoAutenticado().pipe(map((doc) => [doc]));

    doctor$.pipe(finalize(() => (this.cargando = false))).subscribe({
      next: (medicosList) => {
        this.medicos = medicosList;
        this.doctorId = medicosList[0]?.id ?? null;
        const defaultSede = sedeId || this.sedes[0]?.id || null;
        this.baseForm.patchValue({ sedeId: defaultSede });
        this.cargarDisponibilidad();
      },
      error: () => (this.mensajeError = 'No se pudo cargar la disponibilidad.')
    });
  }

  cargarDisponibilidad(): void {
    if (!this.doctorId) {
      this.horariosBase = [];
      this.excepciones = [];
      return;
    }
    this.cargando = true;
    forkJoin({
      bases: this.disponibilidadService.listarBases(this.doctorId),
      excepciones: this.disponibilidadService.listarExcepciones(
        this.doctorId,
        this.filtroExcepcionForm.value.fechaInicio || undefined,
        this.filtroExcepcionForm.value.fechaFin || undefined
      )
    }).pipe(finalize(() => (this.cargando = false))).subscribe({
      next: ({ bases, excepciones }) => {
        this.horariosBase = bases;
        this.excepciones = excepciones;
      },
      error: () => (this.mensajeError = 'No se pudo cargar la disponibilidad del medico.')
    });
  }

  guardarBase(): void {
    if (!this.doctorId || this.baseForm.invalid) {
      this.baseForm.markAllAsTouched();
      return;
    }
    const value = this.baseForm.getRawValue();
    this.guardandoBase = true;
    this.limpiarMensajes();
    this.disponibilidadService.guardarBase(this.doctorId, {
      sedeId: Number(value.sedeId),
      diaSemana: Number(value.diaSemana),
      horaInicio: value.horaInicio || '08:00',
      horaFin: value.horaFin || '13:00'
    }).pipe(finalize(() => (this.guardandoBase = false))).subscribe({
      next: () => {
        this.mensajeExito = 'Horario base guardado correctamente.';
        this.cargarDisponibilidad();
      },
      error: () => (this.mensajeError = 'No se pudo guardar el horario base.')
    });
  }

  guardarExcepcion(): void {
    if (!this.doctorId || this.excepcionForm.invalid) {
      this.excepcionForm.markAllAsTouched();
      return;
    }
    const value = this.excepcionForm.getRawValue();
    this.guardandoExcepcion = true;
    this.limpiarMensajes();
    this.disponibilidadService.crearExcepcion(this.doctorId, {
      fecha: value.fecha || '',
      horaInicio: value.horaInicio || '08:00',
      horaFin: value.horaFin || '09:00',
      motivo: value.motivo || ''
    }).pipe(finalize(() => (this.guardandoExcepcion = false))).subscribe({
      next: () => {
        this.excepcionForm.reset({ fecha: '', horaInicio: '08:00', horaFin: '09:00', motivo: '' });
        this.mensajeExito = 'Excepcion registrada correctamente.';
        this.cargarDisponibilidad();
      },
      error: () => (this.mensajeError = 'No se pudo registrar la excepcion.')
    });
  }

  eliminarBase(base: DisponibilidadBaseResponse): void {
    if (!this.doctorId) {
      return;
    }
    this.disponibilidadService.eliminarBase(this.doctorId, base.id).subscribe({
      next: () => this.cargarDisponibilidad(),
      error: () => (this.mensajeError = 'No se pudo eliminar el horario base.')
    });
  }

  eliminarExcepcion(excepcion: ExcepcionDisponibilidadResponse): void {
    if (!this.doctorId) {
      return;
    }
    this.disponibilidadService.eliminarExcepcion(this.doctorId, excepcion.id).subscribe({
      next: () => this.cargarDisponibilidad(),
      error: () => (this.mensajeError = 'No se pudo eliminar la excepcion.')
    });
  }

  nombreDia(id: number): string {
    return this.dias.find((dia) => dia.id === id)?.nombre ?? String(id);
  }

  private limpiarMensajes(): void {
    this.mensajeError = '';
    this.mensajeExito = '';
  }

  private hoy(): string {
    return new Date().toISOString().slice(0, 10);
  }

  private enTreintaDias(): string {
    const fecha = new Date();
    fecha.setDate(fecha.getDate() + 30);
    return fecha.toISOString().slice(0, 10);
  }
}
