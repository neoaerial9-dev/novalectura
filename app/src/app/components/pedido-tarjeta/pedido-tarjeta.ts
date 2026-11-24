import { DatePipe } from '@angular/common';
import { Component, computed, input } from '@angular/core';
import { Pedido } from '../../core/models';
import { MonedaPipe } from '../../pipes/moneda-pipe';

@Component({
  selector: 'app-pedido-tarjeta',
  imports: [DatePipe, MonedaPipe],
  template: `
    <article class="tarjeta pedido">
      <header class="pedido__cabecera">
        <div>
          <strong>Pedido #{{ pedido().id }}</strong>
          <span class="muted"> · {{ pedido().fechaPedido | date: 'd MMM y, HH:mm' }}</span>
          @if (mostrarCliente()) {
            <div class="muted pedido__cliente">{{ pedido().cliente }} · {{ pedido().correoCliente }}</div>
          }
        </div>
        <div class="pedido__total">
          <span class="insignia">{{ estado() }}</span>
          <span class="precio">{{ pedido().total | moneda }}</span>
        </div>
      </header>
      <div class="tabla-contenedor">
        <table class="tabla">
          <thead>
            <tr>
              <th>Libro</th>
              <th class="num">Cant.</th>
              <th class="num">Precio</th>
              <th class="num">Subtotal</th>
            </tr>
          </thead>
          <tbody>
            @for (d of pedido().detalles; track d.libroId) {
              <tr>
                <td>{{ d.titulo }}</td>
                <td class="num">{{ d.cantidad }}</td>
                <td class="num">{{ d.precioUnitario | moneda }}</td>
                <td class="num">{{ d.subtotal | moneda }}</td>
              </tr>
            }
          </tbody>
        </table>
      </div>
    </article>
  `,
  styles: `
    .pedido {
      padding: 18px;
    }
    .pedido__cabecera {
      display: flex;
      justify-content: space-between;
      gap: 12px;
      flex-wrap: wrap;
      margin-bottom: 12px;
    }
    .pedido__cliente {
      font-size: 0.8125rem;
    }
    .pedido__total {
      display: flex;
      align-items: center;
      gap: 12px;
    }
    .pedido__total .precio {
      font-size: 1.1875rem;
    }
    .tabla-contenedor {
      box-shadow: none;
    }
  `,
})
export class PedidoTarjeta {
  readonly pedido = input.required<Pedido>();
  readonly mostrarCliente = input(false);
  protected readonly estado = computed(() => {
    const e = this.pedido().estado;
    return e.charAt(0) + e.slice(1).toLowerCase();
  });
}
