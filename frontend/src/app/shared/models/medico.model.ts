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
  sedesIds?: number[];
  consultorioIds?: number[];
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
  consultorioIds?: number[];
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
  consultorioIds?: number[];
}
