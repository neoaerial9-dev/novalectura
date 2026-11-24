import { HttpClient } from '@angular/common/http';
import { Service, computed, effect, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Observable, merge, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { Carrito } from '../core/models';
import { ActualizacionesService } from './actualizaciones-service';
import { AuthService } from './auth-service';

const VACIO: Carrito = { items: [], total: 0 };

@Service()
export class CarritoService {
  private readonly http = inject(HttpClient);
  private readonly auth = inject(AuthService);
  private readonly actualizaciones = inject(ActualizacionesService);
  private readonly base = `${environment.apiUrl}/carrito`;

  private readonly _carrito = signal<Carrito>(VACIO);
  readonly carrito = this._carrito.asReadonly();
  readonly cantidadItems = computed(() => this._carrito().items.reduce((suma, i) => suma + i.cantidad, 0));

  constructor() {
    effect(() => {
      if (this.auth.autenticado()) {
        this.cargar().subscribe({ error: () => undefined });
      } else {
        this._carrito.set(VACIO);
      }
    });

    merge(this.actualizaciones.de('catalogo'), this.actualizaciones.deOtros('carrito'))
      .pipe(takeUntilDestroyed())
      .subscribe(() => {
        if (this.auth.autenticado()) {
          this.cargar().subscribe({ error: () => undefined });
        }
      });
  }

  cargar(): Observable<Carrito> {
    return this.http.get<Carrito>(this.base).pipe(tap((c) => this._carrito.set(c)));
  }

  agregar(libroId: number, cantidad: number): Observable<Carrito> {
    return this.http.post<Carrito>(`${this.base}/items`, { libroId, cantidad }).pipe(tap((c) => this.aplicar(c)));
  }

  actualizarCantidad(libroId: number, cantidad: number): Observable<Carrito> {
    return this.http
      .put<Carrito>(`${this.base}/items/${libroId}`, { cantidad })
      .pipe(tap((c) => this.aplicar(c)));
  }

  eliminar(libroId: number): Observable<Carrito> {
    return this.http.delete<Carrito>(`${this.base}/items/${libroId}`).pipe(tap((c) => this.aplicar(c)));
  }

  vaciar(): Observable<void> {
    return this.http.delete<void>(this.base).pipe(tap(() => this.aplicar(VACIO)));
  }

  private aplicar(carrito: Carrito): void {
    this._carrito.set(carrito);
    this.actualizaciones.notificar('carrito');
  }

  reiniciar(): void {
    this._carrito.set(VACIO);
  }
}
