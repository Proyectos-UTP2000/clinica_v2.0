export interface MedicoResponse {
  id: number;
  usuarioId: number;
  dni: string;
  nombres: string;
  apellidos: string;
  email: string;
  telefono?: string;
  fechaNacimiento?: string;
  especialidadNombre: string;
  subespecialidadNombre?: string;
  sedes: string[];
  activo: boolean;
}

export interface MedicoCreateRequest {
  dni: string;
  nombres: string;
  apellidos: string;
  email: string;
  telefono?: string;
  fechaNacimiento?: string;
  especialidadId: number;
  subespecialidadId?: number;
  sedesIds: number[];
}

export interface MedicoUpdateRequest {
  nombres: string;
  apellidos: string;
  email: string;
  telefono?: string;
  fechaNacimiento?: string;
  especialidadId: number;
  subespecialidadId?: number;
  sedesIds: number[];
}
