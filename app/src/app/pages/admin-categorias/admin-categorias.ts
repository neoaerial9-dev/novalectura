import { Component, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { mensajeError } from '../../core/errores';
import { Categoria } from '../../core/models';
import { CategoriaService } from '../../services/categoria-service';
import { ToastService } from '../../services/toast-service';

@Component({
  selector: 'app-admin-categorias',
  imports: [ReactiveFormsModule],
  templateUrl: './admin-categorias.html',
})
export class AdminCategorias {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly servicio = inject(CategoriaService);
  private readonly toast = inject(ToastService);

  protected readonly categorias = signal<Categoria[]>([]);
  protected readonly editando = signal<Categoria | null>(null);
  protected readonly cargando = signal(true);
  protected readonly guardando = signal(false);

  protected readonly form = this.fb.group({
    nombre: ['', [Validators.required, Validators.maxLength(60)]],
    descripcion: ['', Validators.maxLength(255)],
  });

  constructor() {
    this.cargar();
  }

  private cargar(): void {
    this.servicio.listar().subscribe({
      next: (lista) => {
        this.categorias.set(lista);
        this.cargando.set(false);
      },
      error: (e: unknown) => {
        this.cargando.set(false);
        this.toast.error(mensajeError(e, 'No se pudieron cargar las categorías'));
      },
    });
  }

  protected editar(categoria: Categoria): void {
    this.editando.set(categoria);
    this.form.setValue({ nombre: categoria.nombre, descripcion: categoria.descripcion ?? '' });
  }

  protected cancelar(): void {
    this.editando.set(null);
    this.form.reset();
  }

  protected guardar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const { nombre, descripcion } = this.form.getRawValue();
    const solicitud = { nombre: nombre.trim(), descripcion: descripcion.trim() || null };
    const actual = this.editando();
    const peticion = actual ? this.servicio.actualizar(actual.id, solicitud) : this.servicio.crear(solicitud);
    this.guardando.set(true);
    peticion.subscribe({
      next: () => {
        this.toast.exito(actual ? 'Categoría actualizada' : 'Categoría creada');
        this.guardando.set(false);
        this.cancelar();
        this.cargar();
      },
      error: (e: unknown) => {
        this.guardando.set(false);
        this.toast.error(mensajeError(e, 'No se pudo guardar la categoría'));
      },
    });
  }

  protected eliminar(categoria: Categoria): void {
    if (!confirm(`¿Eliminar la categoría "${categoria.nombre}"?`)) {
      return;
    }
    this.servicio.eliminar(categoria.id).subscribe({
      next: () => {
        this.toast.exito('Categoría eliminada');
        this.cargar();
      },
      error: (e: unknown) => this.toast.error(mensajeError(e, 'No se pudo eliminar la categoría')),
    });
  }
}
