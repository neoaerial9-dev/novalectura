import { DatePipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { mensajeError } from '../../core/errores';
import { Venta } from '../../core/models';
import { MonedaPipe } from '../../pipes/moneda-pipe';
import { ActualizacionesService } from '../../services/actualizaciones-service';
import { VentaService } from '../../services/venta-service';

@Component({
  selector: 'app-mis-ventas',
  imports: [DatePipe, RouterLink, MonedaPipe],
  templateUrl: './mis-ventas.html',
})
export class MisVentas {
  private readonly servicio = inject(VentaService);

  protected readonly ventas = signal<Venta[]>([]);
  protected readonly cargando = signal(true);
  protected readonly error = signal<string | null>(null);
  protected readonly totalVendido = computed(() => this.ventas().reduce((suma, v) => suma + v.subtotal, 0));

  constructor() {
    this.cargar();
    inject(ActualizacionesService)
      .de('pedidos')
      .pipe(takeUntilDestroyed())
      .subscribe(() => this.cargar(true));
  }

  private cargar(silencioso = false): void {
    this.servicio.misVentas().subscribe({
      next: (lista) => {
        this.ventas.set(lista);
        this.error.set(null);
        this.cargando.set(false);
      },
      error: (e: unknown) => {
        if (!silencioso) {
          this.error.set(mensajeError(e, 'No se pudieron cargar tus ventas'));
        }
        this.cargando.set(false);
      },
    });
  }
}
