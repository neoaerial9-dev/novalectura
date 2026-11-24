import { Component, computed, inject, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { Icono } from '../../components/icono/icono';
import { mensajeError } from '../../core/errores';
import { ItemCarrito } from '../../core/models';
import { ImagenLibro } from '../../directives/imagen-libro';
import { MonedaPipe } from '../../pipes/moneda-pipe';
import { CarritoService } from '../../services/carrito-service';
import { PedidoService } from '../../services/pedido-service';
import { ToastService } from '../../services/toast-service';

@Component({
  selector: 'app-mi-carrito',
  imports: [RouterLink, Icono, ImagenLibro, MonedaPipe],
  templateUrl: './mi-carrito.html',
  styleUrl: './mi-carrito.scss',
})
export class MiCarrito {
  protected readonly carrito = inject(CarritoService);
  private readonly pedidos = inject(PedidoService);
  private readonly toast = inject(ToastService);
  private readonly router = inject(Router);

  protected readonly cargando = signal(true);
  protected readonly confirmando = signal(false);

  protected readonly hayProblemas = computed(() =>
    this.carrito.carrito().items.some((i) => !i.disponible || i.cantidad > i.stockDisponible),
  );

  constructor() {
    this.carrito.cargar().subscribe({
      next: () => this.cargando.set(false),
      error: (e: unknown) => {
        this.cargando.set(false);
        this.toast.error(mensajeError(e, 'No se pudo cargar el carrito'));
      },
    });
  }

  protected cambiarCantidad(item: ItemCarrito, delta: number): void {
    const nueva = item.cantidad + delta;
    if (nueva < 1) {
      return;
    }
    if (nueva > item.stockDisponible) {
      this.toast.error(`Solo hay ${item.stockDisponible} unidades disponibles`);
      return;
    }
    this.carrito.actualizarCantidad(item.libroId, nueva).subscribe({
      error: (e: unknown) => this.toast.error(mensajeError(e, 'No se pudo actualizar la cantidad')),
    });
  }

  protected quitar(item: ItemCarrito): void {
    this.carrito.eliminar(item.libroId).subscribe({
      error: (e: unknown) => this.toast.error(mensajeError(e, 'No se pudo quitar el libro')),
    });
  }

  protected confirmar(): void {
    if (this.hayProblemas()) {
      return;
    }
    this.confirmando.set(true);
    this.pedidos.registrar().subscribe({
      next: (pedido) => {
        this.carrito.reiniciar();
        this.toast.exito(`Pedido #${pedido.id} registrado`);
        void this.router.navigate(['/mis-pedidos']);
      },
      error: (e: unknown) => {
        this.confirmando.set(false);
        this.toast.error(mensajeError(e, 'No se pudo registrar el pedido'));
        this.carrito.cargar().subscribe({ error: () => undefined });
      },
    });
  }
}
