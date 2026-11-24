import { Component, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Paginador } from '../../components/paginador/paginador';
import { PedidoTarjeta } from '../../components/pedido-tarjeta/pedido-tarjeta';
import { mensajeError } from '../../core/errores';
import { Pedido } from '../../core/models';
import { ActualizacionesService } from '../../services/actualizaciones-service';
import { PedidoService } from '../../services/pedido-service';

@Component({
  selector: 'app-admin-pedidos',
  imports: [Paginador, PedidoTarjeta],
  templateUrl: './admin-pedidos.html',
})
export class AdminPedidos {
  private readonly servicio = inject(PedidoService);

  protected readonly pedidos = signal<Pedido[]>([]);
  protected readonly pagina = signal(0);
  protected readonly totalPaginas = signal(0);
  protected readonly total = signal(0);
  protected readonly cargando = signal(true);
  protected readonly error = signal<string | null>(null);

  constructor() {
    this.cargar(0);
    inject(ActualizacionesService)
      .de('pedidos')
      .pipe(takeUntilDestroyed())
      .subscribe(() => this.cargar(this.pagina(), true));
  }

  protected cargar(pagina: number, silencioso = false): void {
    if (!silencioso) {
      this.cargando.set(true);
    }
    this.servicio.todos(pagina, 10).subscribe({
      next: (r) => {
        this.error.set(null);
        this.pedidos.set(r.content);
        this.pagina.set(r.page.number);
        this.totalPaginas.set(r.page.totalPages);
        this.total.set(r.page.totalElements);
        this.cargando.set(false);
      },
      error: (e: unknown) => {
        if (!silencioso) {
          this.error.set(mensajeError(e, 'No se pudieron cargar los pedidos'));
        }
        this.cargando.set(false);
      },
    });
  }
}
