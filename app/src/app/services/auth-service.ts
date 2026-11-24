import { HttpClient } from '@angular/common/http';
import { Service, computed, inject, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { AuthResponse, LoginRequest, RegistroRequest, Usuario } from '../core/models';
import { ActualizacionesService } from './actualizaciones-service';

const CLAVE_TOKEN = 'nl.token';
const CLAVE_USUARIO = 'nl.usuario';

function leer(clave: string): string | null {
  try {
    return localStorage.getItem(clave);
  } catch {
    return null;
  }
}

function leerUsuario(): Usuario | null {
  const texto = leer(CLAVE_USUARIO);
  if (!texto) {
    return null;
  }
  try {
    return JSON.parse(texto) as Usuario;
  } catch {
    return null;
  }
}

@Service()
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly actualizaciones = inject(ActualizacionesService);
  private readonly base = `${environment.apiUrl}/auth`;

  private readonly _token = signal<string | null>(leer(CLAVE_TOKEN));
  private readonly _usuario = signal<Usuario | null>(leerUsuario());

  readonly token = this._token.asReadonly();
  readonly usuario = this._usuario.asReadonly();
  readonly autenticado = computed(() => this._token() !== null && this._usuario() !== null);
  readonly esAdmin = computed(() => this._usuario()?.rol === 'ADMINISTRADOR');
  readonly iniciales = computed(() => {
    const u = this._usuario();
    return u ? (u.nombres.charAt(0) + u.apellidos.charAt(0)).toUpperCase() : '';
  });

  login(solicitud: LoginRequest): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${this.base}/login`, solicitud)
      .pipe(tap((respuesta) => this.guardar(respuesta)));
  }

  registrar(solicitud: RegistroRequest): Observable<Usuario> {
    return this.http
      .post<Usuario>(`${this.base}/registro`, solicitud)
      .pipe(tap(() => this.actualizaciones.notificar('usuarios')));
  }

  cerrarSesion(): void {
    if (this._token()) {
      this.http.post<void>(`${this.base}/logout`, null).subscribe({ error: () => undefined });
    }
    this.limpiar();
  }

  limpiar(): void {
    this._token.set(null);
    this._usuario.set(null);
    try {
      localStorage.removeItem(CLAVE_TOKEN);
      localStorage.removeItem(CLAVE_USUARIO);
    } catch {
    }
  }

  actualizarUsuario(usuario: Usuario): void {
    this._usuario.set(usuario);
    this.persistir(CLAVE_USUARIO, JSON.stringify(usuario));
  }

  private guardar(respuesta: AuthResponse): void {
    this._token.set(respuesta.token);
    this._usuario.set(respuesta.usuario);
    this.persistir(CLAVE_TOKEN, respuesta.token);
    this.persistir(CLAVE_USUARIO, JSON.stringify(respuesta.usuario));
  }

  private persistir(clave: string, valor: string): void {
    try {
      localStorage.setItem(clave, valor);
    } catch {
    }
  }
}
