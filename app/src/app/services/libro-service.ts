import { HttpClient, HttpParams } from '@angular/common/http';
import { Service, inject, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { Libro, LibroRequest, Pagina } from '../core/models';
import { ActualizacionesService } from './actualizaciones-service';

export interface FiltrosLibros {
  q?: string;
  categoriaId?: number | null;
  page?: number;
  size?: number;
  sort?: string;
}

@Service()
export class LibroService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/libros`;

  private readonly actualizaciones = inject(ActualizacionesService);

  readonly cambios$ = this.actualizaciones.de('catalogo');
  readonly resaltado = signal<number | null>(null);

  notificarCambio(libroId?: number): void {
    this.actualizaciones.notificar('catalogo');
    if (libroId != null) {
      this.resaltado.set(libroId);
      setTimeout(() => this.resaltado.set(null), 4000);
    }
  }

  listar(filtros: FiltrosLibros = {}): Observable<Pagina<Libro>> {
    let params = new HttpParams();
    const q = filtros.q?.trim();
    if (q) {
      params = params.set('q', q);
    }
    if (filtros.categoriaId != null) {
      params = params.set('categoriaId', filtros.categoriaId);
    }
    if (filtros.page != null) {
      params = params.set('page', filtros.page);
    }
    if (filtros.size != null) {
      params = params.set('size', filtros.size);
    }
    if (filtros.sort) {
      params = params.set('sort', filtros.sort);
    }
    return this.http.get<Pagina<Libro>>(this.base, { params });
  }

  obtener(id: number): Observable<Libro> {
    return this.http.get<Libro>(`${this.base}/${id}`);
  }

  crear(solicitud: LibroRequest): Observable<Libro> {
    return this.http.post<Libro>(this.base, solicitud).pipe(tap((libro) => this.notificarCambio(libro.id)));
  }

  actualizar(id: number, solicitud: LibroRequest): Observable<Libro> {
    return this.http.put<Libro>(`${this.base}/${id}`, solicitud).pipe(tap((libro) => this.notificarCambio(libro.id)));
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`).pipe(tap(() => this.notificarCambio()));
  }

  misLibros(): Observable<Libro[]> {
    return this.http.get<Libro[]>(`${environment.apiUrl}/mis-libros`);
  }
}
