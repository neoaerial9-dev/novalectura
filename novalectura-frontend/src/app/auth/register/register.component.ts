import { HttpErrorResponse } from '@angular/common/http';
import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { RegisterRequest } from '../../interfaces/auth.interface';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-register',
  standalone: false,
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss']
})
export class RegisterComponent {
  request: RegisterRequest = {
    nombres: '',
    apellidos: '',
    correo: '',
    contrasena: ''
  };
  confirmPassword = '';
  errorMessage = '';
  loading = false;

  constructor(
    private authService: AuthService,
    private router: Router
  ) { }

  register(): void {
    if (this.loading) {
      return;
    }

    if (this.request.contrasena !== this.confirmPassword) {
      this.errorMessage = 'Las contraseñas no coinciden.';
      return;
    }

    this.errorMessage = '';
    this.loading = true;
    this.authService.register({
      ...this.request,
      nombres: this.request.nombres.trim(),
      apellidos: this.request.apellidos.trim(),
      correo: this.request.correo.trim()
    }).subscribe({
      next: () => {
        this.loading = false;
        this.router.navigate(['/catalogo']);
      },
      error: (error: HttpErrorResponse) => {
        const detail: unknown = error.error?.detail;
        this.errorMessage = typeof detail === 'string' && detail.trim()
          ? detail
          : 'No se pudo crear la cuenta. Verifica tus datos e inténtalo de nuevo.';
        this.loading = false;
      }
    });
  }
}
