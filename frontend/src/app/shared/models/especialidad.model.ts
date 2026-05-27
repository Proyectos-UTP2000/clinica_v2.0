export interface EspecialidadResponse {
  id: number;
  nombre: string;
  descripcion?: string;
  especialidadPadreId?: number;
  especialidadPadreNombre?: string;
}
