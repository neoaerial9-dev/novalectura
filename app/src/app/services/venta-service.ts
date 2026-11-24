import { HttpClient } from '@angular/common/http';
import { Service, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Venta } from '../core/models';

@Service()
export class VentaService {
  private readonly http = inject(HttpClient);

  misVentas(): Observable<Venta[]> {
    return this.http.get<Venta[]>(`${environment.apiUrl}/mis-ventas`);
  }
}
