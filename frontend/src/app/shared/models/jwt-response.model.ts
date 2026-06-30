export interface JwtResponse {
  token: string;
  dni: string;
  nombres: string;
  apellidos: string;
  cambioPasswordObligatorio: boolean;
  roles: string[];
  permisos: string[];
  email?: string;
  telefono?: string;
}
