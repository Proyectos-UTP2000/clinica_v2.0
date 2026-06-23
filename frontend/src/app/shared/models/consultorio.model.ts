export interface ConsultorioResponse {
  id: number;
  sedeId: number;
  sedeNombre: string;
  nombre: string;
  piso?: string;
  area?: string;
  activo: boolean;
}

export interface ConsultorioCreateRequest {
  sedeId: number;
  nombre: string;
  piso?: string;
  area?: string;
}

export interface ConsultorioUpdateRequest {
  nombre: string;
  piso?: string;
  area?: string;
}
