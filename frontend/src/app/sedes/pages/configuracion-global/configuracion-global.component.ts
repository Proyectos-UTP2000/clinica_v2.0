import { Component, OnInit } from '@angular/core';
import { forkJoin, finalize } from 'rxjs';
import { SedeService } from '../../services/sede.service';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-configuracion-global',
  templateUrl: './configuracion-global.component.html',
  styleUrl: './configuracion-global.component.css',
  standalone: false
})
export class ConfiguracionGlobalComponent implements OnInit {
  configs: any[] = [];
  cargando = false;
  guardando = false;
  mensajeError = '';
  mensajeExito = '';

  // Modelo de campos editables vinculados por comodidad
  valores: { [key: string]: string } = {
    'clinica.nombre': '',
    'clinica.razon_social': '',
    'clinica.direccion': '',
    'clinica.telefono': ''
  };

  constructor(
    public readonly authService: AuthService,
    private readonly sedeService: SedeService
  ) {}

  ngOnInit(): void {
    this.cargarConfiguracion();
  }

  cargarConfiguracion(): void {
    this.cargando = true;
    this.mensajeError = '';
    this.sedeService.obtenerConfiguracionGlobal()
      .pipe(finalize(() => this.cargando = false))
      .subscribe({
        next: (lista) => {
          this.configs = lista;
          // Normalizar mapeo para campos definidos
          lista.forEach(item => {
            if (item.clave in this.valores) {
              this.valores[item.clave] = item.valor;
            }
          });
        },
        error: () => {
          this.mensajeError = 'Error al cargar los valores de configuración global.';
        }
      });
  }

  guardar(): void {
    this.guardando = true;
    this.mensajeError = '';
    this.mensajeExito = '';

    // Generar las llamadas PUT correspondientes
    const llamadas = Object.keys(this.valores).map(clave => 
      this.sedeService.actualizarConfiguracionGlobal(clave, this.valores[clave])
    );

    forkJoin(llamadas)
      .pipe(finalize(() => this.guardando = false))
      .subscribe({
        next: () => {
          this.mensajeExito = 'Configuración global guardada correctamente.';
          this.cargarConfiguracion(); // Recargar
        },
        error: (err) => {
          this.mensajeError = err.error?.mensaje || 'Ocurrió un error al intentar guardar la configuración.';
        }
      });
  }
}
