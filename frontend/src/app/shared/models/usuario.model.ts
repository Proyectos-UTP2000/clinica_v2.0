import { RolResponse } from './rol.model';

export interface UsuarioResponse {
  id: number;
  dni: string;
  nombres: string;
  apellidos: string;
  email: string;
  telefono?: string;
  fechaNacimiento?: string;
  activo: boolean;
  roles: RolResponse[];
  doctorIds: number[];
}

export interface UsuarioCreateRequest {
  dni: string;
  nombres: string;
  apellidos: string;
  email: string;
  telefono?: string;
  fechaNacimiento?: string;
  rolesIds: number[];
  doctorIds?: number[];
}

export interface UsuarioUpdateRequest {
  nombres: string;
  apellidos: string;
  email: string;
  telefono?: string;
  fechaNacimiento?: string;
  rolesIds: number[];
  doctorIds?: number[];
}
