import { HttpErrorResponse } from '@angular/common/http';
import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: false,
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent {
  correo = '';
  contrasena = '';
  errorMessage = '';
  loading = false;

  constructor(
    private authService: AuthService,
    private router: Router
  ) { }

  login(): void {
    if (this.loading) {
      return;
    }

    this.errorMessage = '';
    this.loading = true;
    this.authService.login(this.correo, this.contrasena).subscribe(
      () => {
        this.loading = false;
        this.router.navigate([this.authService.isAdmin() ? '/admin/libros' : '/catalogo']);
      },
      (error: HttpErrorResponse) => {
        this.loading = false;
        this.errorMessage = error.error && error.error.detail
          ? error.error.detail
          : 'No se pudo iniciar sesión. Verifica tus datos e inténtalo de nuevo.';
      }
    );
  }
}