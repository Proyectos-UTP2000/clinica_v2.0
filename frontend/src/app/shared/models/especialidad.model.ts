export interface EspecialidadResponse {
  id: number;
  nombre: string;
  descripcion?: string;
  especialidadPadreId?: number;
  especialidadPadreNombre?: string;
}

export interface EspecialidadCreateRequest {
  nombre: string;
  descripcion?: string;
  especialidadPadreId?: number | null;
}

export interface EspecialidadUpdateRequest {
  nombre: string;
  descripcion?: string;
  especialidadPadreId?: number | null;
}
