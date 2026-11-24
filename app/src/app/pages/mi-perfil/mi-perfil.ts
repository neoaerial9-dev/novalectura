import { Component, inject, signal } from '@angular/core';
import { AbstractControl, NonNullableFormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { mensajeError } from '../../core/errores';
import { AuthService } from '../../services/auth-service';
import { ToastService } from '../../services/toast-service';
import { UsuarioService } from '../../services/usuario-service';

function distintaDeLaActual(grupo: AbstractControl): ValidationErrors | null {
  const actual = grupo.get('actual')?.value;
  return actual && actual === grupo.get('nueva')?.value ? { igual: true } : null;
}

@Component({
  selector: 'app-mi-perfil',
  imports: [ReactiveFormsModule],
  templateUrl: './mi-perfil.html',
  styleUrl: './mi-perfil.scss',
})
export class MiPerfil {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly usuarios = inject(UsuarioService);
  private readonly toast = inject(ToastService);
  protected readonly auth = inject(AuthService);

  protected readonly guardando = signal(false);
  protected readonly cambiando = signal(false);

  protected readonly form = this.fb.group({
    nombres: ['', [Validators.required, Validators.maxLength(80)]],
    apellidos: ['', [Validators.required, Validators.maxLength(80)]],
    telefono: ['', Validators.maxLength(20)],
    direccionEnvio: ['', Validators.maxLength(200)],
  });

  protected readonly formClave = this.fb.group(
    {
      actual: ['', Validators.required],
      nueva: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(64)]],
    },
    { validators: distintaDeLaActual },
  );

  constructor() {
    this.usuarios.miPerfil().subscribe({
      next: (u) => {
        this.auth.actualizarUsuario(u);
        this.form.setValue({
          nombres: u.nombres,
          apellidos: u.apellidos,
          telefono: u.telefono ?? '',
          direccionEnvio: u.direccionEnvio ?? '',
        });
      },
      error: (e: unknown) => this.toast.error(mensajeError(e, 'No se pudo cargar tu perfil')),
    });
  }

  protected guardar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const v = this.form.getRawValue();
    this.guardando.set(true);
    this.usuarios
      .actualizarPerfil({
        nombres: v.nombres.trim(),
        apellidos: v.apellidos.trim(),
        telefono: v.telefono.trim() || null,
        direccionEnvio: v.direccionEnvio.trim() || null,
      })
      .subscribe({
        next: (u) => {
          this.auth.actualizarUsuario(u);
          this.guardando.set(false);
          this.toast.exito('Perfil actualizado');
        },
        error: (e: unknown) => {
          this.guardando.set(false);
          this.toast.error(mensajeError(e, 'No se pudo actualizar el perfil'));
        },
      });
  }

  protected cambiarContrasena(): void {
    if (this.formClave.invalid) {
      this.formClave.markAllAsTouched();
      return;
    }
    this.cambiando.set(true);
    this.usuarios.cambiarContrasena(this.formClave.getRawValue()).subscribe({
      next: () => {
        this.cambiando.set(false);
        this.formClave.reset();
        this.toast.exito('Contraseña actualizada');
      },
      error: (e: unknown) => {
        this.cambiando.set(false);
        this.toast.error(mensajeError(e, 'No se pudo cambiar la contraseña'));
      },
    });
  }
}
