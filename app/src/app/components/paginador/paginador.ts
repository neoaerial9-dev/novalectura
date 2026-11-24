import { Component, input, output } from '@angular/core';
import { Icono } from '../icono/icono';

@Component({
  selector: 'app-paginador',
  imports: [Icono],
  template: `
    @if (totalPaginas() > 1) {
      <nav class="paginador" aria-label="Paginación">
        <button type="button" class="btn btn--borde btn--sm" [disabled]="pagina() <= 0" (click)="cambiar.emit(pagina() - 1)">
          <app-icono nombre="izquierda" [tamano]="14" /> Anterior
        </button>
        <span class="muted">Página {{ pagina() + 1 }} de {{ totalPaginas() }}</span>
        <button
          type="button"
          class="btn btn--borde btn--sm"
          [disabled]="pagina() >= totalPaginas() - 1"
          (click)="cambiar.emit(pagina() + 1)"
        >
          Siguiente <app-icono nombre="derecha" [tamano]="14" />
        </button>
      </nav>
    }
  `,
  styles: `
    .paginador {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 16px;
      margin-top: 28px;
      font-size: 0.8125rem;
    }
  `,
})
export class Paginador {
  readonly pagina = input.required<number>();
  readonly totalPaginas = input.required<number>();
  readonly cambiar = output<number>();
}
