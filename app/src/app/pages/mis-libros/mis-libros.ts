import { Component, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { mensajeError } from '../../core/errores';
import { Libro } from '../../core/models';
import { ImagenLibro } from '../../directives/imagen-libro';
import { CondicionPipe } from '../../pipes/condicion-pipe';
import { MonedaPipe } from '../../pipes/moneda-pipe';
import { LibroService } from '../../services/libro-service';
import { ToastService } from '../../services/toast-service';

@Component({
  selector: 'app-mis-libros',
  imports: [RouterLink, ImagenLibro, CondicionPipe, MonedaPipe],
  templateUrl: './mis-libros.html',
})
export class MisLibros {
  private readonly servicio = inject(LibroService);
  private readonly toast = inject(ToastService);

  protected readonly libros = signal<Libro[]>([]);
  protected readonly cargando = signal(true);
  protected readonly error = signal<string | null>(null);
  protected readonly resaltado = this.servicio.resaltado;

  constructor() {
    this.cargar();
    this.servicio.cambios$.pipe(takeUntilDestroyed()).subscribe(() => this.cargar());
  }

  private cargar(): void {
    this.servicio.misLibros().subscribe({
      next: (lista) => {
        this.libros.set(lista);
        this.cargando.set(false);
      },
      error: (e: unknown) => {
        this.error.set(mensajeError(e, 'No se pudieron cargar tus libros'));
        this.cargando.set(false);
      },
    });
  }

  protected eliminar(libro: Libro): void {
    if (!confirm(`¿Eliminar "${libro.titulo}"? Dejará de mostrarse en el catálogo.`)) {
      return;
    }
    this.servicio.eliminar(libro.id).subscribe({
      next: () => {
        this.toast.exito('Libro eliminado');
      },
      error: (e: unknown) => this.toast.error(mensajeError(e, 'No se pudo eliminar el libro')),
    });
  }
}
