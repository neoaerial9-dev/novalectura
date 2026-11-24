import { Component } from '@angular/core';

@Component({
  selector: 'app-footer',
  template: `
    <footer class="pie">
      <div class="contenedor">
        <span>© 2026 Tienda de Libros · Nova Lectura S.A.C. · Arequipa, Perú</span>
        <span>Libros usados con una segunda vida</span>
      </div>
    </footer>
  `,
  styles: `
    .pie {
      border-top: 1px solid var(--borde);
      padding: 22px 0;
      color: var(--muted);
      font-size: 0.8125rem;
    }
    .contenedor {
      display: flex;
      justify-content: space-between;
      gap: 12px;
      flex-wrap: wrap;
    }
  `,
})
export class Footer {}
