import { Component, OnInit } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { finalize } from 'rxjs';
import { ConsultaResponse } from '../../../shared/models/consulta.model';
import { HistorialService } from '../../services/historial.service';

@Component({
    selector: 'app-ver-consulta',
    templateUrl: './ver-consulta.component.html',
    standalone: false
})
export class VerConsultaComponent implements OnInit {
  consulta?: ConsultaResponse;
  cargando = false;
  guardandoNota = false;
  mensajeError = '';
  mensajeExito = '';

  notaForm = this.fb.group({
    nota: ['', Validators.required]
  });

  constructor(
    private readonly route: ActivatedRoute,
    private readonly fb: FormBuilder,
    private readonly historialService: HistorialService
  ) {}

  ngOnInit(): void {
    this.cargarConsulta(Number(this.route.snapshot.paramMap.get('id')));
  }

  agregarNota(): void {
    if (!this.consulta || this.notaForm.invalid) {
      this.notaForm.markAllAsTouched();
      return;
    }
    this.guardandoNota = true;
    this.mensajeError = '';
    this.mensajeExito = '';
    this.historialService.agregarNota(this.consulta.id, this.notaForm.value.nota || '')
      .pipe(finalize(() => (this.guardandoNota = false)))
      .subscribe({
        next: (consulta) => {
          this.consulta = consulta;
          this.notaForm.reset();
          this.mensajeExito = 'Nota agregada correctamente.';
        },
        error: () => (this.mensajeError = 'No se pudo agregar la nota.')
      });
  }

  private cargarConsulta(id: number): void {
    this.cargando = true;
    this.historialService.obtener(id)
      .pipe(finalize(() => (this.cargando = false)))
      .subscribe({
        next: (consulta) => (this.consulta = consulta),
        error: () => (this.mensajeError = 'No se pudo cargar la consulta.')
      });
  }
}
