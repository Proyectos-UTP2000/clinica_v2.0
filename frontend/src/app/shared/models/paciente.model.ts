export interface PacienteResponse {
  id: number;
  dni: string;
  nombres: string;
  apellidos: string;
  sexo?: string;
  fechaNacimiento: string;
  telefono: string;
  email?: string;
  activo: boolean;
}

export interface PacienteCreateRequest {
  dni: string;
  nombres: string;
  apellidos: string;
  sexo?: string;
  fechaNacimiento: string;
  telefono: string;
  email?: string;
}

export interface PacienteUpdateRequest {
  nombres: string;
  apellidos: string;
  sexo?: string;
  fechaNacimiento: string;
  telefono: string;
  email?: string;
}
