export interface ConsultaResponse {
  id: number;
  pacienteId: number;
  pacienteNombre: string;
  doctorId: number;
  doctorNombre: string;
  sedeId: number;
  sedeNombre: string;
  citaId?: number;
  fechaHora: string;
  tipo: string;
  motivoConsulta?: string;
  diagnostico?: string;
  observaciones?: string;
  estado: string;
  recetas: RecetaResponse[];
  indicaciones: IndicacionMedicaResponse[];
  estudios: EstudioComplementarioResponse[];
  adjuntos: AdjuntoResponse[];
  notasEvolucion: NotaEvolucionResponse[];
}

export interface RecetaResponse {
  id: number;
  medicamento: string;
  dosis?: string;
  frecuencia?: string;
  duracion?: string;
  indicaciones?: string;
}

export interface IndicacionMedicaResponse {
  id: number;
  tipo: string;
  descripcion: string;
}

export interface EstudioComplementarioResponse {
  id: number;
  tipoEstudio: string;
  detalle: string;
  estado: string;
  archivoResultado?: string;
  pacienteNombre?: string;
  pacienteDni?: string;
  consultaId?: number;
  fechaHora?: string;
}


export interface AdjuntoResponse {
  id: number;
  nombreArchivo: string;
  tipoMime: string;
  fechaSubida: string;
  ruta: string;
}

export interface NotaEvolucionResponse {
  id: number;
  fecha: string;
  nota: string;
  autorId?: number;
  autorNombre: string;
}

export interface ConsultaCreateRequest {
  pacienteId: number;
  doctorId: number;
  sedeId: number;
  citaId?: number;
  tipo: string;
  motivoConsulta?: string;
  diagnostico?: string;
  observaciones?: string;
  recetas?: RecetaRequest[];
  indicaciones?: IndicacionRequest[];
  estudios?: EstudioRequest[];
  adjuntos?: AdjuntoRequest[];
}

export interface RecetaRequest {
  medicamento: string;
  dosis?: string;
  frecuencia?: string;
  duracion?: string;
  indicaciones?: string;
}

export interface IndicacionRequest {
  tipo: string;
  descripcion: string;
}

export interface EstudioRequest {
  tipoEstudio: string;
  detalle: string;
}

export interface AdjuntoRequest {
  nombreArchivo: string;
  tipoMime: string;
  ruta: string;
}
