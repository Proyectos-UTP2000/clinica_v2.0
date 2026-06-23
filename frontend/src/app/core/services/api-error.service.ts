import { HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class ApiErrorService {
  /**
   * Extrae el mensaje de error legible desde una respuesta HTTP del backend
   * o de un error genérico.
   */
  obtenerMensajeError(error: any): string {
    if (!error) {
      return 'Ocurrió un error inesperado.';
    }

    if (error instanceof HttpErrorResponse) {
      // Intentar extraer de la estructura ApiResponse del backend (propiedad mensaje)
      if (error.error && typeof error.error === 'object') {
        if ('mensaje' in error.error && typeof error.error.mensaje === 'string') {
          return error.error.mensaje;
        }
        if ('message' in error.error && typeof error.error.message === 'string') {
          return error.error.message;
        }
      }
      
      // Errores de red o conexión caída (status 0)
      if (error.status === 0) {
        return 'No se pudo establecer conexión con el servidor. Por favor, verifique si el backend está en ejecución.';
      }

      // Errores HTTP genéricos por código de estado
      if (error.status === 401) {
        return 'Sesión expirada o credenciales inválidas.';
      }
      if (error.status === 403) {
        return 'No tiene permisos para realizar esta acción.';
      }
      if (error.status === 404) {
        return 'El recurso solicitado no fue encontrado.';
      }

      return `Error del servidor (${error.status}): ${error.statusText || 'Error desconocido'}`;
    }

    if (typeof error === 'string') {
      return error;
    }

    return error.message || 'Ocurrió un error inesperado.';
  }
}
