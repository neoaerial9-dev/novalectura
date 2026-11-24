import { HttpClient } from '@angular/common/http';
import { Service, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Categoria, CategoriaRequest } from '../core/models';

@Service()
export class CategoriaService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/categorias`;

  listar(): Observable<Categoria[]> {
    return this.http.get<Categoria[]>(this.base);
  }

  crear(solicitud: CategoriaRequest): Observable<Categoria> {
    return this.http.post<Categoria>(this.base, solicitud);
  }

  actualizar(id: number, solicitud: CategoriaRequest): Observable<Categoria> {
    return this.http.put<Categoria>(`${this.base}/${id}`, solicitud);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
