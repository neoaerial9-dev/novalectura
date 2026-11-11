export interface IUser {
  idUsuario: number;
  nombres: string;
  apellidos: string;
  correo: string;
  rol: string;
  telefono: string | null;
  direccion: string | null;
  fechaRegistro: string;
}

export interface UserProfileRequest {
  nombres: string;
  apellidos: string;
  telefono: string | null;
  direccion: string | null;
}