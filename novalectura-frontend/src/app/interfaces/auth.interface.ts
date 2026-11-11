import { IUser } from './user.interface';

export interface AuthResponse {
  token: string;
  tipo: string;
  usuario: IUser;
}

export interface RegisterRequest {
  nombres: string;
  apellidos: string;
  correo: string;
  contrasena: string;
}

export interface ChangePasswordRequest {
  contrasenaActual: string;
  contrasenaNueva: string;
}
