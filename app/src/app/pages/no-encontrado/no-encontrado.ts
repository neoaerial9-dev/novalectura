import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-no-encontrado',
  imports: [RouterLink],
  template: `
    <div class="contenedor pagina pagina--estrecha">
      <div class="vacio tarjeta">
        <span class="eyebrow">Error 404</span>
        <h2>No encontramos esa página</h2>
        <p>Puede que el enlace esté roto o la página ya no exista.</p>
        <a routerLink="/" class="btn btn--primario">Volver al inicio</a>
      </div>
    </div>
  `,
})
export class NoEncontrado {}
