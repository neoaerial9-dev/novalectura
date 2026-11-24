import { Component, OnInit, inject, input, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { mensajeError } from '../../core/errores';
import { validarImagen } from '../../core/imagen';
import { CONDICIONES, CONDICION_ETIQUETA, Categoria, CondicionLibro, LibroRequest } from '../../core/models';
import { ImagenLibro } from '../../directives/imagen-libro';
import { CategoriaService } from '../../services/categoria-service';
import { ImagenService } from '../../services/imagen-service';
import { LibroService } from '../../services/libro-service';
import { ToastService } from '../../services/toast-service';

function nulo(texto: string): string | null {
  return texto.trim() === '' ? null : texto.trim();
}

@Component({
  selector: 'app-formulario-libro',
  imports: [ReactiveFormsModule, RouterLink, ImagenLibro],
  templateUrl: './formulario-libro.html',
  styleUrl: './formulario-libro.scss',
})
export class FormularioLibro implements OnInit {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly libros = inject(LibroService);
  private readonly categoriasService = inject(CategoriaService);
  private readonly imagenes = inject(ImagenService);
  private readonly router = inject(Router);
  private readonly toast = inject(ToastService);

  readonly id = input<string>();

  protected readonly condiciones = CONDICIONES.map((valor) => ({ valor, etiqueta: CONDICION_ETIQUETA[valor] }));
  protected readonly categorias = signal<Categoria[]>([]);
  protected readonly cargando = signal(false);
  protected readonly guardando = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly subiendo = signal(false);
  protected readonly errorImagen = signal<string | null>(null);

  protected readonly form = this.fb.group({
    titulo: ['', [Validators.required, Validators.maxLength(150)]],
    autor: ['', [Validators.required, Validators.maxLength(120)]],
    editorial: ['', Validators.maxLength(100)],
    isbn: ['', Validators.maxLength(20)],
    anioPublicacion: [null as number | null, [Validators.min(1000), Validators.max(2100)]],
    descripcion: ['', Validators.maxLength(2000)],
    precio: [null as number | null, [Validators.required, Validators.min(0.01)]],
    stock: [null as number | null, [Validators.required, Validators.min(0)]],
    imagen: ['', Validators.maxLength(500)],
    condicion: ['BUENO' as CondicionLibro, Validators.required],
    categoriaId: [null as number | null, Validators.required],
  });

  protected get esEdicion(): boolean {
    return !!this.id();
  }

  constructor() {
    this.categoriasService.listar().subscribe({
      next: (lista) => this.categorias.set(lista),
      error: (e: unknown) => this.error.set(mensajeError(e, 'No se pudieron cargar las categorías')),
    });
  }

  ngOnInit(): void {
    const id = this.id();
    if (!id) {
      return;
    }
    this.cargando.set(true);
    this.libros.obtener(Number(id)).subscribe({
      next: (libro) => {
        this.form.setValue({
          titulo: libro.titulo,
          autor: libro.autor,
          editorial: libro.editorial ?? '',
          isbn: libro.isbn ?? '',
          anioPublicacion: libro.anioPublicacion,
          descripcion: libro.descripcion ?? '',
          precio: libro.precio,
          stock: libro.stock,
          imagen: libro.imagen ?? '',
          condicion: libro.condicion,
          categoriaId: libro.categoria.id,
        });
        this.cargando.set(false);
      },
      error: (e: unknown) => {
        this.error.set(mensajeError(e, 'No se pudo cargar el libro'));
        this.cargando.set(false);
      },
    });
  }

  protected elegirImagen(evento: Event): void {
    const campo = evento.target as HTMLInputElement;
    const archivo = campo.files?.[0];
    if (!archivo) {
      return;
    }
    const motivo = validarImagen(archivo);
    if (motivo) {
      this.errorImagen.set(motivo);
      campo.value = '';
      return;
    }
    this.errorImagen.set(null);
    this.subiendo.set(true);
    this.imagenes.subir(archivo).subscribe({
      next: (ruta) => {
        this.form.controls.imagen.setValue(ruta);
        this.subiendo.set(false);
        campo.value = '';
      },
      error: (e: unknown) => {
        this.errorImagen.set(mensajeError(e, 'No se pudo subir la imagen'));
        this.subiendo.set(false);
        campo.value = '';
      },
    });
  }

  protected quitarImagen(): void {
    this.form.controls.imagen.setValue('');
  }

  protected enviar(): void {
    const v = this.form.getRawValue();
    if (this.form.invalid || v.precio == null || v.stock == null || v.categoriaId == null) {
      this.form.markAllAsTouched();
      return;
    }
    const solicitud: LibroRequest = {
      titulo: v.titulo.trim(),
      autor: v.autor.trim(),
      editorial: nulo(v.editorial),
      isbn: nulo(v.isbn),
      anioPublicacion: v.anioPublicacion,
      descripcion: nulo(v.descripcion),
      precio: v.precio,
      stock: v.stock,
      imagen: nulo(v.imagen),
      condicion: v.condicion,
      categoriaId: v.categoriaId,
    };
    const id = this.id();
    const peticion = id ? this.libros.actualizar(Number(id), solicitud) : this.libros.crear(solicitud);
    this.guardando.set(true);
    this.error.set(null);
    peticion.subscribe({
      next: () => {
        this.toast.exito(id ? 'Libro actualizado' : 'Libro publicado');
        void this.router.navigate(['/vender']);
      },
      error: (e: unknown) => {
        this.guardando.set(false);
        this.error.set(mensajeError(e, 'No se pudo guardar el libro'));
      },
    });
  }
}
