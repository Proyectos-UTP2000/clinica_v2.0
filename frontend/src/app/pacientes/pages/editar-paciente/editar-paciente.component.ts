import { Component, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { finalize } from 'rxjs';
import { PacienteResponse, PacienteUpdateRequest } from '../../../shared/models/paciente.model';
import { PacienteService } from '../../services/paciente.service';

@Component({
    selector: 'app-editar-paciente',
    templateUrl: './editar-paciente.component.html',
    styleUrl: './editar-paciente.component.css',
    standalone: false
})
export class EditarPacienteComponent implements OnInit {
  paciente: PacienteResponse | null = null;
  cargando = false;
  guardando = false;
  mensajeError = '';

  pacienteForm = this.fb.group({
    nombres: ['', [Validators.required, Validators.maxLength(80)]],
    apellidos: ['', [Validators.required, Validators.maxLength(80)]],
    sexo: [''],
    fechaNacimiento: ['', [Validators.required]],
    telefono: ['', [Validators.required, Validators.pattern(/^\d{7,15}$/)]],
    email: ['', [Validators.email]]
  });

  constructor(
    private readonly fb: FormBuilder,
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly pacienteService: PacienteService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.cargarPaciente(id);
  }

  guardar(): void {
    if (!this.paciente) {
      return;
    }

    this.mensajeError = '';
    this.pacienteForm.markAllAsTouched();
    if (this.pacienteForm.invalid) {
      return;
    }

    this.guardando = true;
    this.pacienteService.actualizar(this.paciente.id, this.construirRequest())
      .pipe(finalize(() => (this.guardando = false)))
      .subscribe({
        next: () => this.router.navigateByUrl('/pacientes'),
        error: () => {
          this.mensajeError = 'No se pudo actualizar el paciente.';
        }
      });
  }

  campoInvalido(campo: string): boolean {
    const control = this.pacienteForm.get(campo);
    return Boolean(control?.invalid && (control.dirty || control.touched));
  }

  private cargarPaciente(id: number): void {
    this.cargando = true;
    this.pacienteService.obtenerPorId(id)
      .pipe(finalize(() => (this.cargando = false)))
      .subscribe({
        next: (paciente) => {
          this.paciente = paciente;
          this.pacienteForm.patchValue({
            nombres: paciente.nombres,
            apellidos: paciente.apellidos,
            sexo: paciente.sexo ?? '',
            fechaNacimiento: paciente.fechaNacimiento,
            telefono: paciente.telefono,
            email: paciente.email ?? ''
          });
        },
        error: () => {
          this.mensajeError = 'No se pudo cargar el paciente solicitado.';
        }
      });
  }

  private construirRequest(): PacienteUpdateRequest {
    const raw = this.pacienteForm.getRawValue();
    return {
      nombres: raw.nombres ?? '',
      apellidos: raw.apellidos ?? '',
      sexo: raw.sexo || undefined,
      fechaNacimiento: raw.fechaNacimiento ?? '',
      telefono: raw.telefono ?? '',
      email: raw.email || undefined
    };
  }
}
