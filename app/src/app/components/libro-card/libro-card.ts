import { Component, computed, inject, input, output } from '@angular/core';
import { Libro } from '../../core/models';
import { ImagenLibro } from '../../directives/imagen-libro';
import { CondicionPipe } from '../../pipes/condicion-pipe';
import { MonedaPipe } from '../../pipes/moneda-pipe';
import { AuthService } from '../../services/auth-service';

@Component({
  selector: 'app-libro-card',
  imports: [ImagenLibro, CondicionPipe, MonedaPipe],
  templateUrl: './libro-card.html',
  styleUrl: './libro-card.scss',
})
export class LibroCard {
  private readonly auth = inject(AuthService);

  readonly libro = input.required<Libro>();
  readonly abrir = output<Libro>();
  readonly agregar = output<Libro>();

  protected readonly esPropio = computed(() => this.auth.usuario()?.id === this.libro().vendedorId);
  protected readonly agotado = computed(() => this.libro().stock <= 0);
  protected readonly etiquetaBoton = computed(() => (this.esPropio() ? 'Tuyo' : this.agotado() ? 'Agotado' : '+ Carrito'));
}
