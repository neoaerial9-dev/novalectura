import { Component, inject, input, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Subject, catchError, debounceTime, distinctUntilChanged, filter, fromEvent, interval, merge, of, switchMap, tap } from 'rxjs';
import { Icono } from '../../components/icono/icono';
import { LibroCard } from '../../components/libro-card/libro-card';
import { LibroDetalle } from '../../components/libro-detalle/libro-detalle';
import { Paginador } from '../../components/paginador/paginador';
import { mensajeError } from '../../core/errores';
import { Categoria, Libro } from '../../core/models';
import { AuthService } from '../../services/auth-service';
import { CarritoService } from '../../services/carrito-service';
import { CategoriaService } from '../../services/categoria-service';
import { LibroService } from '../../services/libro-service';
import { ToastService } from '../../services/toast-service';

const TAMANO_PAGINA = 12;
const REFRESCO_MS = 30_000;

const ORDENES = [
  { valor: 'id,desc', etiqueta: 'Más recientes' },
  { valor: 'precio,asc', etiqueta: 'Precio: menor a mayor' },
  { valor: 'precio,desc', etiqueta: 'Precio: mayor a menor' },
  { valor: 'titulo,asc', etiqueta: 'Título (A-Z)' },
] as const;

const pestanaVisible = (): boolean => document.visibilityState !== 'hidden';

@Component({
  selector: 'app-catalogo',
  imports: [ReactiveFormsModule, Icono, LibroCard, LibroDetalle, Paginador],
  templateUrl: './catalogo.html',
  styleUrl: './catalogo.scss',
})
export class Catalogo {
  private readonly libroService = inject(LibroService);
  private readonly categoriaService = inject(CategoriaService);
  private readonly carrito = inject(CarritoService);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly toast = inject(ToastService);

  readonly hero = input(false);

  protected readonly busqueda = new FormControl('', { nonNullable: true });
  protected readonly categoria = new FormControl<number | null>(null);
  protected readonly orden = new FormControl<string>(ORDENES[0].valor, { nonNullable: true });
  protected readonly ordenes = ORDENES;

  protected readonly categorias = signal<Categoria[]>([]);
  protected readonly libros = signal<Libro[]>([]);
  protected readonly total = signal(0);
  protected readonly totalPaginas = signal(0);
  protected readonly pagina = signal(0);
  protected readonly cargando = signal(true);
  protected readonly error = signal<string | null>(null);
  protected readonly seleccionado = signal<Libro | null>(null);

  private readonly consulta$ = new Subject<boolean>();

  constructor() {
    this.consulta$
      .pipe(
        tap((silencioso) => {
          if (!silencioso) {
            this.cargando.set(true);
            this.error.set(null);
          }
        }),
        switchMap((silencioso) =>
          this.libroService
            .listar({
              q: this.busqueda.value,
              categoriaId: this.categoria.value,
              sort: this.orden.value,
              page: this.pagina(),
              size: TAMANO_PAGINA,
            })
            .pipe(
              catchError((e: unknown) => {
                if (!silencioso) {
                  this.error.set(mensajeError(e, 'No se pudo cargar el catálogo'));
                }
                return of(null);
              }),
            ),
        ),
        takeUntilDestroyed(),
      )
      .subscribe((respuesta) => {
        this.cargando.set(false);
        if (!respuesta) {
          return;
        }
        const { totalPages, totalElements } = respuesta.page;
        if (respuesta.content.length === 0 && this.pagina() > 0 && totalPages > 0) {
          this.pagina.set(totalPages - 1);
          this.consulta$.next(true);
          return;
        }
        this.libros.set(respuesta.content);
        this.total.set(totalElements);
        this.totalPaginas.set(totalPages);
      });

    merge(
      this.busqueda.valueChanges.pipe(debounceTime(300), distinctUntilChanged()),
      this.categoria.valueChanges,
      this.orden.valueChanges,
    )
      .pipe(takeUntilDestroyed())
      .subscribe(() => {
        this.pagina.set(0);
        this.consulta$.next(false);
      });

    merge(
      this.libroService.cambios$,
      interval(REFRESCO_MS).pipe(filter(pestanaVisible)),
      fromEvent(document, 'visibilitychange').pipe(filter(pestanaVisible)),
    )
      .pipe(takeUntilDestroyed())
      .subscribe(() => this.consulta$.next(true));

    this.categoriaService.listar().subscribe({ next: (c) => this.categorias.set(c), error: () => undefined });
    this.consulta$.next(false);
  }

  protected cambiarPagina(pagina: number): void {
    this.pagina.set(pagina);
    this.consulta$.next(false);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  protected agregarAlCarrito(libro: Libro, cantidad = 1): void {
    if (!this.auth.autenticado()) {
      void this.router.navigate(['/ingresar'], { queryParams: { redirect: this.router.url } });
      return;
    }
    this.carrito.agregar(libro.id, cantidad).subscribe({
      next: () => {
        this.toast.exito(`"${libro.titulo}" se agregó al carrito`);
        this.seleccionado.set(null);
      },
      error: (e: unknown) => this.toast.error(mensajeError(e, 'No se pudo agregar al carrito')),
    });
  }

  protected editar(libro: Libro): void {
    void this.router.navigate(['/vender', libro.id, 'editar']);
  }

  protected eliminar(libro: Libro): void {
    if (!confirm(`¿Eliminar "${libro.titulo}" del catálogo?`)) {
      return;
    }
    this.libroService.eliminar(libro.id).subscribe({
      next: () => {
        this.toast.exito('Libro eliminado');
        this.seleccionado.set(null);
      },
      error: (e: unknown) => this.toast.error(mensajeError(e, 'No se pudo eliminar el libro')),
    });
  }
}
