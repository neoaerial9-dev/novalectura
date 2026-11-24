import { Component, inject, input, signal } from '@angular/core';
import { AbstractControl, NonNullableFormBuilder, ReactiveFormsModule, ValidationErrors, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { switchMap } from 'rxjs';
import { Icono } from '../../components/icono/icono';
import { destinoSeguro, mensajeError } from '../../core/errores';
import { AuthService } from '../../services/auth-service';

function contrasenasCoinciden(grupo: AbstractControl): ValidationErrors | null {
  return grupo.get('contrasena')?.value === grupo.get('confirmar')?.value ? null : { noCoinciden: true };
}

@Component({
  selector: 'app-registro',
  imports: [ReactiveFormsModule, RouterLink, Icono],
  templateUrl: './registro.html',
})
export class Registro {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  readonly redirect = input<string>();

  protected readonly form = this.fb.group(
    {
      nombres: ['', [Validators.required, Validators.maxLength(80)]],
      apellidos: ['', [Validators.required, Validators.maxLength(80)]],
      correo: ['', [Validators.required, Validators.email]],
      contrasena: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(64)]],
      confirmar: ['', Validators.required],
    },
    { validators: contrasenasCoinciden },
  );
  protected readonly enviando = signal(false);
  protected readonly error = signal<string | null>(null);

  protected enviar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const { nombres, apellidos, correo, contrasena } = this.form.getRawValue();
    this.enviando.set(true);
    this.error.set(null);
    this.auth
      .registrar({ nombres, apellidos, correo, contrasena })
      .pipe(switchMap(() => this.auth.login({ correo, contrasena })))
      .subscribe({
        next: () => void this.router.navigateByUrl(destinoSeguro(this.redirect())),
        error: (e: unknown) => {
          this.error.set(mensajeError(e, 'No se pudo crear la cuenta'));
          this.enviando.set(false);
        },
      });
  }
}
