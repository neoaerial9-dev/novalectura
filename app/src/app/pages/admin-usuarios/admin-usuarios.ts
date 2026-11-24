import { DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Paginador } from '../../components/paginador/paginador';
import { mensajeError } from '../../core/errores';
import { Usuario } from '../../core/models';
import { ActualizacionesService } from '../../services/actualizaciones-service';
import { UsuarioService } from '../../services/usuario-service';

@Component({
  selector: 'app-admin-usuarios',
  imports: [DatePipe, Paginador],
  templateUrl: './admin-usuarios.html',
})
export class AdminUsuarios {
  private readonly servicio = inject(UsuarioService);

  protected readonly usuarios = signal<Usuario[]>([]);
  protected readonly pagina = signal(0);
  protected readonly totalPaginas = signal(0);
  protected readonly total = signal(0);
  protected readonly cargando = signal(true);
  protected readonly error = signal<string | null>(null);

  constructor() {
    this.cargar(0);
    inject(ActualizacionesService)
      .de('usuarios')
      .pipe(takeUntilDestroyed())
      .subscribe(() => this.cargar(this.pagina(), true));
  }

  protected cargar(pagina: number, silencioso = false): void {
    if (!silencioso) {
      this.cargando.set(true);
    }
    this.servicio.listar(pagina, 10).subscribe({
      next: (r) => {
        this.error.set(null);
        this.usuarios.set(r.content);
        this.pagina.set(r.page.number);
        this.totalPaginas.set(r.page.totalPages);
        this.total.set(r.page.totalElements);
        this.cargando.set(false);
      },
      error: (e: unknown) => {
        if (!silencioso) {
          this.error.set(mensajeError(e, 'No se pudieron cargar los usuarios'));
        }
        this.cargando.set(false);
      },
    });
  }
}
