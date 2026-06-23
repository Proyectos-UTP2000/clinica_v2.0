export interface CitaResponse {
  id: number;
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
}

export interface CitaCreateRequest {
  pacienteId: number;
  doctorId: number;
  sedeId: number;
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
