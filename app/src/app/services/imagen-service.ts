import { HttpClient } from '@angular/common/http';
import { Service, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { environment } from '../../environments/environment';

@Service()
export class ImagenService {
  private readonly http = inject(HttpClient);

  subir(archivo: File): Observable<string> {
    const datos = new FormData();
    datos.append('archivo', archivo);
    return this.http.post<{ ruta: string }>(`${environment.apiUrl}/imagenes`, datos).pipe(map((r) => r.ruta));
  }
}
