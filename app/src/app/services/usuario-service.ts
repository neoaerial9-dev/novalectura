import { HttpClient, HttpParams } from '@angular/common/http';
import { Service, inject } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { CambioContrasenaRequest, PerfilRequest, Pagina, Usuario } from '../core/models';
import { ActualizacionesService } from './actualizaciones-service';

@Service()
export class UsuarioService {
  private readonly http = inject(HttpClient);
  private readonly actualizaciones = inject(ActualizacionesService);
  private readonly base = `${environment.apiUrl}/usuarios`;

  miPerfil(): Observable<Usuario> {
    return this.http.get<Usuario>(`${this.base}/me`);
  }

  actualizarPerfil(solicitud: PerfilRequest): Observable<Usuario> {
    return this.http
      .put<Usuario>(`${this.base}/me`, solicitud)
      .pipe(tap(() => this.actualizaciones.notificar('usuarios')));
  }

  cambiarContrasena(solicitud: CambioContrasenaRequest): Observable<void> {
    return this.http.put<void>(`${this.base}/me/password`, solicitud);
  }

  listar(page = 0, size = 10): Observable<Pagina<Usuario>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<Pagina<Usuario>>(this.base, { params });
  }
}
