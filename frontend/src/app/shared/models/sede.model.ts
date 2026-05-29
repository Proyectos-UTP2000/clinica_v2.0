export interface SedeResponse {
  id: number;
  nombre: string;
  direccion: string;
  activo: boolean;
}

export interface SedeCreateRequest {
  nombre: string;
  direccion?: string;
}

export interface SedeUpdateRequest {
  nombre: string;
  direccion?: string;
}
