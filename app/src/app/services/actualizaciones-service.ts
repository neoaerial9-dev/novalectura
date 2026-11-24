import { Service } from '@angular/core';
import { Observable, Subject, filter, map } from 'rxjs';

export type Recurso = 'catalogo' | 'pedidos' | 'usuarios' | 'carrito';

const RECURSOS: readonly Recurso[] = ['catalogo', 'pedidos', 'usuarios', 'carrito'];

@Service()
export class ActualizacionesService {
  private readonly _cambios = new Subject<Recurso>();
  private readonly _externos = new Subject<Recurso>();
  private readonly _locales = new Subject<Recurso>();

  readonly locales$ = this._locales.asObservable();

  de(recurso: Recurso): Observable<void> {
    return this._cambios.pipe(
      filter((r) => r === recurso),
      map(() => undefined),
    );
  }

  deOtros(recurso: Recurso): Observable<void> {
    return this._externos.pipe(
      filter((r) => r === recurso),
      map(() => undefined),
    );
  }

  notificar(recurso: Recurso): void {
    this._cambios.next(recurso);
    this._locales.next(recurso);
  }

  notificarExterno(recurso: Recurso): void {
    this._cambios.next(recurso);
    this._externos.next(recurso);
  }

  esRecurso(valor: unknown): valor is Recurso {
    return typeof valor === 'string' && (RECURSOS as readonly string[]).includes(valor);
  }
}
