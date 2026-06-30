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
    styleUrl: './gestion-disponibilidad.component.css',
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

  // Variables para la organización de la vista
  pestanaActiva = 'base';
  mostrarModalBase = false;
  mostrarModalExcepcion = false;

  dias = [
    { id: 1, nombre: 'Lunes' },
    { id: 2, nombre: 'Martes' },
    { id: 3, nombre: 'Miércoles' },
    { id: 4, nombre: 'Jueves' },
    { id: 5, nombre: 'Viernes' },
    { id: 6, nombre: 'Sábado' },
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
    this.sesionContextService.sedes$.subscribe((sedes) => {
      this.sedes = sedes;
      this.sesionContextService.selectedSedeId$.subscribe((sedeId) => {
        this.cargarParaSede(sedeId);
      });
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

  get sedesDelMedicoSeleccionado(): SedeResponse[] {
    if (!this.doctorId) {
      return [];
    }
    const medico = this.medicos.find(m => m.id === this.doctorId);
    if (!medico || !medico.sedesIds) {
      return this.sedes;
    }
    return medico.sedesIds.map((id, index) => ({
      id,
      nombre: medico.sedes[index] || 'Sede',
      direccion: '',
      activo: true
    }));
  }

  abrirModalBase(): void {
    this.limpiarMensajes();
    this.mostrarModalBase = true;
    setTimeout(() => {
      const list = this.sedesDelMedicoSeleccionado;
      const defaultSede = this.sesionContextService.selectedSedeId || (list.length > 0 ? list[0].id : null);
      this.baseForm.patchValue({
        sedeId: defaultSede,
        diaSemana: 1,
        horaInicio: '08:00',
        horaFin: '13:00'
      });
      this.baseForm.markAsPristine();
      this.baseForm.markAsUntouched();
    }, 0);
  }

  abrirModalExcepcion(): void {
    this.limpiarMensajes();
    this.mostrarModalExcepcion = true;
    setTimeout(() => {
      this.excepcionForm.patchValue({
        fecha: this.hoy(),
        horaInicio: '08:00',
        horaFin: '09:00',
        motivo: ''
      });
      this.excepcionForm.markAsPristine();
      this.excepcionForm.markAsUntouched();
    }, 0);
  }

  guardarBase(): void {
    this.limpiarMensajes();
    if (!this.doctorId) {
      this.mensajeError = 'No se ha seleccionado ningún médico.';
      return;
    }
    if (this.baseForm.invalid) {
      this.baseForm.markAllAsTouched();
      const invalidFields = [];
      if (this.baseForm.get('sedeId')?.invalid) invalidFields.push('Sede');
      if (this.baseForm.get('diaSemana')?.invalid) invalidFields.push('Día');
      if (this.baseForm.get('horaInicio')?.invalid) invalidFields.push('Hora Inicio');
      if (this.baseForm.get('horaFin')?.invalid) invalidFields.push('Hora Fin');
      this.mensajeError = 'Por favor complete todos los campos obligatorios: ' + invalidFields.join(', ') + '.';
      return;
    }
    const value = this.baseForm.getRawValue();
    this.guardandoBase = true;
    this.disponibilidadService.guardarBase(this.doctorId, {
      sedeId: Number(value.sedeId),
      diaSemana: Number(value.diaSemana),
      horaInicio: value.horaInicio || '08:00',
      horaFin: value.horaFin || '13:00'
    }).pipe(finalize(() => (this.guardandoBase = false))).subscribe({
      next: () => {
        this.mensajeExito = 'Horario base guardado correctamente.';
        this.mostrarModalBase = false;
        this.cargarDisponibilidad();
      },
      error: (err) => (this.mensajeError = err.error?.mensaje || 'No se pudo guardar el horario base.')
    });
  }

  guardarExcepcion(): void {
    this.limpiarMensajes();
    if (!this.doctorId) {
      this.mensajeError = 'No se ha seleccionado ningún médico.';
      return;
    }
    if (this.excepcionForm.invalid) {
      this.excepcionForm.markAllAsTouched();
      const invalidFields = [];
      if (this.excepcionForm.get('fecha')?.invalid) invalidFields.push('Fecha');
      if (this.excepcionForm.get('horaInicio')?.invalid) invalidFields.push('Hora Inicio');
      if (this.excepcionForm.get('horaFin')?.invalid) invalidFields.push('Hora Fin');
      if (this.excepcionForm.get('motivo')?.invalid) invalidFields.push('Motivo');
      this.mensajeError = 'Por favor complete todos los campos obligatorios: ' + invalidFields.join(', ') + '.';
      return;
    }
    const value = this.excepcionForm.getRawValue();
    this.guardandoExcepcion = true;
    this.disponibilidadService.crearExcepcion(this.doctorId, {
      fecha: value.fecha || '',
      horaInicio: value.horaInicio || '08:00',
      horaFin: value.horaFin || '09:00',
      motivo: value.motivo || ''
    }).pipe(finalize(() => (this.guardandoExcepcion = false))).subscribe({
      next: () => {
        this.mensajeExito = 'Excepción registrada correctamente.';
        this.mostrarModalExcepcion = false;
        this.cargarDisponibilidad();
      },
      error: (err) => (this.mensajeError = err.error?.mensaje || 'No se pudo registrar la excepción.')
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
      error: () => (this.mensajeError = 'No se pudo eliminar la excepción.')
    });
  }

  nombreDia(id: number): string {
    return this.dias.find((dia) => dia.id === id)?.nombre ?? String(id);
  }

  limpiarMensajes(): void {
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
