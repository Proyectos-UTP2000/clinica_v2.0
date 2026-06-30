import { Component, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { finalize, forkJoin } from 'rxjs';
import { AuthService } from '../../../core/services/auth.service';
import { CitaResponse } from '../../../shared/models/cita.model';
import { PacienteResponse } from '../../../shared/models/paciente.model';
import { PagoResponse } from '../../../shared/models/pago.model';
import { PagoService } from '../../services/pago.service';

@Component({
    selector: 'app-listar-pagos',
    templateUrl: './listar-pagos.component.html',
    styleUrl: './listar-pagos.component.css',
    standalone: false
})
export class ListarPagosComponent implements OnInit {
  pacientes: PacienteResponse[] = [];
  citasPendientes: CitaResponse[] = [];
  pagos: PagoResponse[] = [];
  cargando = false;
  guardando = false;
  mensajeError = '';
  mensajeExito = '';

  busquedaPaciente = '';
  mostrarDropdownPaciente = false;

  get pacientesFiltrados(): PacienteResponse[] {
    const q = this.busquedaPaciente.toLowerCase().trim();
    if (!q) {
      return this.pacientes;
    }
    return this.pacientes.filter(
      p => p.nombres.toLowerCase().includes(q) || p.apellidos.toLowerCase().includes(q) || p.dni.includes(q)
    );
  }

  seleccionarPaciente(paciente: PacienteResponse | null): void {
    if (paciente) {
      this.filtroForm.patchValue({ pacienteId: paciente.id });
      this.busquedaPaciente = `${paciente.apellidos}, ${paciente.nombres} (${paciente.dni})`;
    } else {
      this.filtroForm.patchValue({ pacienteId: null });
      this.busquedaPaciente = '';
    }
    this.mostrarDropdownPaciente = false;
  }

  ocultarDropdownPacienteConRetraso(): void {
    setTimeout(() => {
      this.mostrarDropdownPaciente = false;
    }, 200);
  }

  filtroForm = this.fb.group({
    pacienteId: [null as number | null, Validators.required]
  });

  pagoForm = this.fb.group({
    citaId: [null as number | null, Validators.required],
    monto: [80, Validators.required],
    metodo: ['efectivo', Validators.required]
  });

  constructor(
    public readonly authService: AuthService,
    private readonly pagoService: PagoService,
    private readonly fb: FormBuilder
  ) {}

  ngOnInit(): void {
    this.cargarDatosIniciales();
  }

  cargarDatosIniciales(): void {
    this.cargando = true;
    this.mensajeError = '';
    forkJoin({
      pacientes: this.pagoService.listarPacientes(),
      citas: this.pagoService.listarCitas()
    })
      .pipe(finalize(() => (this.cargando = false)))
      .subscribe({
        next: ({ pacientes, citas }) => {
          this.pacientes = pacientes;
          this.citasPendientes = citas.filter((cita) => cita.estadoPago !== 'pagado');
          const primerPaciente = pacientes[0]?.id ?? null;
          this.filtroForm.patchValue({ pacienteId: primerPaciente });
          if (primerPaciente) {
            const firstPac = pacientes[0];
            this.busquedaPaciente = `${firstPac.apellidos}, ${firstPac.nombres} (${firstPac.dni})`;
            this.cargarPagos();
          }
        },
        error: () => (this.mensajeError = 'No se pudo cargar la información de pagos.')
      });
  }

  cargarPagos(): void {
    const pacienteId = this.filtroForm.value.pacienteId;
    if (!pacienteId) {
      this.pagos = [];
      return;
    }
    this.cargando = true;
    this.pagoService.listarPorPaciente(pacienteId)
      .pipe(finalize(() => (this.cargando = false)))
      .subscribe({
        next: (pagos) => (this.pagos = pagos),
        error: () => (this.mensajeError = 'No se pudieron cargar los pagos del paciente.')
      });
  }

  registrarPago(): void {
    if (this.pagoForm.invalid) {
      this.pagoForm.markAllAsTouched();
      return;
    }
    const value = this.pagoForm.getRawValue();
    this.guardando = true;
    this.mensajeError = '';
    this.mensajeExito = '';
    this.pagoService.registrar({
      citaId: Number(value.citaId),
      monto: Number(value.monto),
      metodo: value.metodo || 'efectivo'
    })
      .pipe(finalize(() => (this.guardando = false)))
      .subscribe({
        next: () => {
          this.mensajeExito = 'Pago registrado correctamente.';
          this.pagoForm.reset({ citaId: null, monto: 80, metodo: 'efectivo' });
          this.cargarDatosIniciales();
        error: (err) => {
          this.mensajeError = err.error?.message || 'No se pudo registrar el pago.';
        }
      });
  }
}
