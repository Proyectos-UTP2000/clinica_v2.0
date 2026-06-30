export interface DisponibilidadBaseResponse {
  id: number;
  doctorId: number;
  sedeId: number;
  sedeNombre: string;
  diaSemana: number;
  horaInicio: string;
  horaFin: string;
  consultorioId?: number;
  consultorioNombre?: string;
}

export interface ExcepcionDisponibilidadResponse {
  id: number;
  doctorId: number;
  fecha: string;
  horaInicio: string;
  horaFin: string;
  motivo: string;
}

export interface DisponibilidadBaseCreateRequest {
  sedeId: number;
  diaSemana: number;
  horaInicio: string;
  horaFin: string;
  consultorioId?: number;
}

export interface ExcepcionDisponibilidadCreateRequest {
  fecha: string;
  horaInicio: string;
  horaFin: string;
  motivo: string;
}
