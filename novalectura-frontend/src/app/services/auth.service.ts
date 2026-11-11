import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { finalize, tap } from 'rxjs/operators';
import { environment } from '../../enviroment/enviroment';
import {
  AuthResponse,
  ChangePasswordRequest,
  RegisterRequest
} from '../interfaces/auth.interface';
import { LocalStorageService } from './local-storage.service';

@Injectable({ providedIn: 'root' })
export class AuthService {
  constructor(
    private http: HttpClient,
    private localStorage: LocalStorageService
  ) { }

  login(correo: string, contrasena: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${environment.apiUrl}/auth/login`, {
      correo,
      contrasena
    }).pipe(
      tap(response => {
        this.localStorage.setToken(response.token);
        this.localStorage.setUser(response.usuario);
      })
    );
  }

  register(request: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${environment.apiUrl}/auth/register`, request).pipe(
      tap(response => {
        this.localStorage.setToken(response.token);
        this.localStorage.setUser(response.usuario);
      })
    );
  }

  logout(): Observable<void> {
    return this.http.post<void>(`${environment.apiUrl}/auth/logout`, {}).pipe(
      finalize(() => this.localStorage.clearSession())
    );
  }

  changePassword(request: ChangePasswordRequest): Observable<void> {
    return this.http.put<void>(`${environment.apiUrl}/auth/password`, request);
  }

  isAdmin(): boolean {
    return this.localStorage.getUser()?.rol === 'ADMIN';
  }
}