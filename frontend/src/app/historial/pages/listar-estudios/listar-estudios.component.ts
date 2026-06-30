import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { HistorialService } from '../../services/historial.service';
import { EstudioComplementarioResponse } from '../../../shared/models/consulta.model';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-listar-estudios',
  templateUrl: './listar-estudios.component.html',
  styleUrls: ['./listar-estudios.component.css'],
  standalone: false
})

export class ListarEstudiosComponent implements OnInit {
  estudios: EstudioComplementarioResponse[] = [];
  filtroForm!: FormGroup;
  cargando = false;
  mensajeError = '';
  mensajeExito = '';

  // Paginación
  page = 0;
  size = 10;
  totalPages = 0;
  totalElements = 0;

  // Manejo de modal/upload
  estudioSeleccionado: EstudioComplementarioResponse | null = null;
  archivosSeleccionados: File[] = [];
  mostrarModalUpload = false;

  constructor(
    private readonly fb: FormBuilder,
    private readonly historialService: HistorialService,
    public readonly authService: AuthService
  ) {}

  ngOnInit(): void {
    this.filtroForm = this.fb.group({
      estado: ['pendiente'],
      filtro: ['']
    });

    this.cargarEstudios();
  }

  cargarEstudios(page = 0): void {
    this.page = page;
    this.cargando = true;
    this.mensajeError = '';
    this.mensajeExito = '';

    const { estado, filtro } = this.filtroForm.value;

    this.historialService.listarEstudios(estado, filtro, this.page, this.size).subscribe({
      next: (res) => {
        this.estudios = res.content;
        this.totalPages = res.totalPages;
        this.totalElements = res.totalElements;
        this.cargando = false;
      },
      error: (err) => {
        this.mensajeError = 'Error al cargar los estudios complementarios';
        this.cargando = false;
      }
    });
  }

  abrirUpload(estudio: EstudioComplementarioResponse): void {
    this.estudioSeleccionado = estudio;
    this.archivosSeleccionados = [];
    this.mostrarModalUpload = true;
  }

  cerrarUpload(): void {
    this.mostrarModalUpload = false;
    this.estudioSeleccionado = null;
    this.archivosSeleccionados = [];
  }

  onFileSelected(event: any): void {
    const files = event.target.files;
    if (files && files.length > 0) {
      this.archivosSeleccionados = Array.from(files);
    }
  }

  subirResultado(): void {
    if (!this.estudioSeleccionado || this.archivosSeleccionados.length === 0) {
      return;
    }

    this.cargando = true;
    this.historialService.subirResultadoEstudio(this.estudioSeleccionado.id, this.archivosSeleccionados).subscribe({
      next: () => {
        this.mensajeExito = 'Resultado(s) cargado(s) correctamente';
        this.cerrarUpload();
        this.cargarEstudios(this.page);
      },
      error: (err) => {
        this.mensajeError = 'Error al subir los archivos de resultado';
        this.cargando = false;
      }
    });
  }

  descargarResultado(estudioId: number, index: number, nombre: string): void {
    this.historialService.descargarResultadoEstudio(estudioId, index).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = nombre;
        a.click();
        window.URL.revokeObjectURL(url);
      },
      error: () => {
        this.mensajeError = 'Error al descargar el resultado del estudio';
      }
    });
  }

  obtenerArchivos(archivoResultado?: string): { nombre: string, index: number }[] {
    if (!archivoResultado) {
      return [];
    }
    return archivoResultado.split(',').map((url, i) => {
      const parts = url.split('/');
      const nombreConId = parts[parts.length - 1];
      return {
        nombre: nombreConId || `resultado_${i + 1}`,
        index: i
      };
    });
  }
}
