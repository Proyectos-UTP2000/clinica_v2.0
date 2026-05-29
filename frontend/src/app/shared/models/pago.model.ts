export interface PagoResponse {
  id: number;
  citaId: number;
  monto: number;
  metodo: string;
  fechaPago: string;
  registradoPorId?: number;
  registradoPor?: string;
}

export interface PagoCreateRequest {
  citaId: number;
  monto: number;
  metodo: string;
}
