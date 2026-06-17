export interface PermisoResponse {
  id: number;
  codigo: string;
  descripcion?: string;
}

export interface RolResponse {
  id: number;
  nombre: string;
  descripcion?: string;
  activo: boolean;
  permisos: PermisoResponse[];
}

export interface RolCreateRequest {
  nombre: string;
  descripcion?: string;
  permisosIds: number[];
}

export interface RolUpdateRequest {
  nombre: string;
  descripcion?: string;
  activo?: boolean;
  permisosIds: number[];
}
