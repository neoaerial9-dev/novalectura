import { Component, inject, input, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { Icono } from '../../components/icono/icono';
import { destinoSeguro, mensajeError } from '../../core/errores';
import { AuthService } from '../../services/auth-service';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, RouterLink, Icono],
  templateUrl: './login.html',
})
export class Login {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  readonly redirect = input<string>();
  readonly expirada = input<string>();

  protected readonly form = this.fb.group({
    correo: ['', [Validators.required, Validators.email]],
    contrasena: ['', Validators.required],
  });
  protected readonly enviando = signal(false);
  protected readonly error = signal<string | null>(null);

  protected enviar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.enviando.set(true);
    this.error.set(null);
    this.auth.login(this.form.getRawValue()).subscribe({
      next: () => void this.router.navigateByUrl(destinoSeguro(this.redirect())),
      error: (e: unknown) => {
        this.error.set(mensajeError(e, 'No se pudo iniciar sesión'));
        this.enviando.set(false);
      },
    });
  }
}
