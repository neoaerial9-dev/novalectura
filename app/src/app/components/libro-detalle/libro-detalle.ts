import { Component, computed, inject, input, output, signal } from '@angular/core';
import { Libro } from '../../core/models';
import { ImagenLibro } from '../../directives/imagen-libro';
import { CondicionPipe } from '../../pipes/condicion-pipe';
import { MonedaPipe } from '../../pipes/moneda-pipe';
import { AuthService } from '../../services/auth-service';
import { Icono } from '../icono/icono';

@Component({
  selector: 'app-libro-detalle',
  imports: [Icono, ImagenLibro, CondicionPipe, MonedaPipe],
  templateUrl: './libro-detalle.html',
  styleUrl: './libro-detalle.scss',
  host: { '(document:keydown.escape)': 'cerrar.emit()' },
})
export class LibroDetalle {
  private readonly auth = inject(AuthService);

  readonly libro = input.required<Libro>();
  readonly cerrar = output<void>();
  readonly agregar = output<number>();
  readonly editar = output<Libro>();
  readonly eliminar = output<Libro>();

  protected readonly cantidad = signal(1);
  protected readonly esPropio = computed(() => this.auth.usuario()?.id === this.libro().vendedorId);
  protected readonly puedeGestionar = computed(() => this.esPropio() || this.auth.esAdmin());
  protected readonly agotado = computed(() => this.libro().stock <= 0);

  protected cambiar(delta: number): void {
    const nueva = this.cantidad() + delta;
    if (nueva >= 1 && nueva <= this.libro().stock) {
      this.cantidad.set(nueva);
    }
  }
}
