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
}

export interface CitaCreateRequest {
  pacienteId: number;
  doctorId: number;
  sedeId: number;
  fechaHoraInicio: string;
}

export interface CitaUpdateRequest {
  nuevaFechaHora: string;
}

export interface DisponibilidadSlotResponse {
  inicio: string;
  fin: string;
}
