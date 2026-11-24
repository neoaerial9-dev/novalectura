import { HttpClient, HttpParams } from '@angular/common/http';
import { Service, inject } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { Pagina, Pedido } from '../core/models';
import { ActualizacionesService } from './actualizaciones-service';

@Service()
export class PedidoService {
  private readonly http = inject(HttpClient);
  private readonly actualizaciones = inject(ActualizacionesService);
  private readonly base = `${environment.apiUrl}/pedidos`;

  registrar(): Observable<Pedido> {
    return this.http.post<Pedido>(this.base, null).pipe(
      tap(() => {
        this.actualizaciones.notificar('catalogo');
        this.actualizaciones.notificar('pedidos');
        this.actualizaciones.notificar('carrito');
      }),
    );
  }

  misPedidos(): Observable<Pedido[]> {
    return this.http.get<Pedido[]>(`${this.base}/mis-pedidos`);
  }

  todos(page = 0, size = 10): Observable<Pagina<Pedido>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<Pagina<Pedido>>(this.base, { params });
  }
}
