import { Component, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { PedidoTarjeta } from '../../components/pedido-tarjeta/pedido-tarjeta';
import { mensajeError } from '../../core/errores';
import { Pedido } from '../../core/models';
import { ActualizacionesService } from '../../services/actualizaciones-service';
import { PedidoService } from '../../services/pedido-service';

@Component({
  selector: 'app-mis-pedidos',
  imports: [RouterLink, PedidoTarjeta],
  templateUrl: './mis-pedidos.html',
})
export class MisPedidos {
  private readonly servicio = inject(PedidoService);

  protected readonly pedidos = signal<Pedido[]>([]);
  protected readonly cargando = signal(true);
  protected readonly error = signal<string | null>(null);

  constructor() {
    this.cargar();
    inject(ActualizacionesService)
      .de('pedidos')
      .pipe(takeUntilDestroyed())
      .subscribe(() => this.cargar(true));
  }

  private cargar(silencioso = false): void {
    this.servicio.misPedidos().subscribe({
      next: (lista) => {
        this.pedidos.set(lista);
        this.error.set(null);
        this.cargando.set(false);
      },
      error: (e: unknown) => {
        if (!silencioso) {
          this.error.set(mensajeError(e, 'No se pudieron cargar tus pedidos'));
        }
        this.cargando.set(false);
      },
    });
  }
}
