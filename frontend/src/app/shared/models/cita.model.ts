export interface CitaResponse {
  id: number;
  pacienteId: number;
  doctorId: number;
  sedeId: number;
  pacienteNombre: string;
  doctorNombre: string;
  sedeNombre: string;
  fechaHoraInicio: string;
  fechaHoraFin: string;
  estado: string;
  estadoPago: string;
  origen: string;
  pagoAnticipado?: boolean;
  beneficiosPagoAnticipado?: boolean;
  reprogramacionesRestantes?: number;
  consultorioId?: number;
  consultorioNombre?: string;
}

export interface CitaCreateRequest {
  pacienteId: number;
  doctorId: number;
  sedeId: number;
  consultorioId: number;
  fechaHoraInicio: string;
  pagoAnticipado?: boolean;
}

export interface CitaUpdateRequest {
  nuevaFechaHora: string;
  doctorId?: number;
}

export interface DisponibilidadSlotResponse {
  inicio: string;
  fin: string;
}
