import { Injectable } from '@angular/core';
import { IUser } from '../interfaces/user.interface';

@Injectable()
export class LocalStorageService {
  private readonly tokenKey = 'novalectura_token';
  private readonly userKey = 'novalectura_user';

  constructor() { }

  setToken(token: string): void {
    localStorage.setItem(this.tokenKey, token);
  }

  setUser(user: IUser): void {
    localStorage.setItem(this.userKey, JSON.stringify(user));
  }

  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  getUser(): IUser | null {
    const storedUser = localStorage.getItem(this.userKey);
    if (!storedUser) {
      return null;
    }

    try {
      const user: unknown = JSON.parse(storedUser);
      if (typeof user === 'object' && user !== null && 'rol' in user
        && typeof user.rol === 'string'
        && 'correo' in user && typeof user.correo === 'string'
        && 'idUsuario' in user && typeof user.idUsuario === 'number'
        && 'nombres' in user && typeof user.nombres === 'string'
        && 'apellidos' in user && typeof user.apellidos === 'string'
        && 'telefono' in user && (typeof user.telefono === 'string' || user.telefono === null)
        && 'direccion' in user && (typeof user.direccion === 'string' || user.direccion === null)
        && 'fechaRegistro' in user && typeof user.fechaRegistro === 'string') {
        return user as IUser;
      }
    } catch (error) {
      if (!(error instanceof SyntaxError)) {
        throw error;
      }
    }

    this.clearSession();
    return null;
  }

  clearSession(): void {
    localStorage.removeItem(this.tokenKey);
    localStorage.removeItem(this.userKey);
  }
}