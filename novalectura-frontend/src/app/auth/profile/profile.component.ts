import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { NgForm } from '@angular/forms';
import { Router } from '@angular/router';
import { ChangePasswordRequest } from '../../interfaces/auth.interface';
import { IUser, UserProfileRequest } from '../../interfaces/user.interface';
import { AuthService } from '../../services/auth.service';
import { LocalStorageService } from '../../services/local-storage.service';
import { UserService } from '../../services/user.service';

@Component({
  selector: 'app-profile',
  standalone: false,
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.css']
})
export class ProfileComponent implements OnInit {
  user: IUser | null = null;
  profile: UserProfileRequest = {
    nombres: '',
    apellidos: '',
    telefono: '',
    direccion: ''
  };
  loadingProfile = true;
  savingProfile = false;
  profileError = '';
  profileSuccess = '';
  password: ChangePasswordRequest = {
    contrasenaActual: '',
    contrasenaNueva: ''
  };
  confirmPassword = '';
  passwordError = '';
  passwordSuccess = '';
  logoutError = '';
  savingPassword = false;
  loggingOut = false;

  constructor(
    private userService: UserService,
    private authService: AuthService,
    private localStorage: LocalStorageService,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.loadProfile();
  }

  loadProfile(): void {
    this.loadingProfile = true;
    this.profileError = '';
    this.userService.getMyProfile().subscribe({
      next: user => {
        this.setProfile(user);
        this.loadingProfile = false;
      },
      error: () => {
        this.profileError = 'No pudimos cargar tus datos. Inténtalo de nuevo.';
        this.loadingProfile = false;
      }
    });
  }

  saveProfile(form: NgForm): void {
    if (this.savingProfile) {
      return;
    }

    this.profileError = '';
    this.profileSuccess = '';
    if (!this.profile.nombres.trim() || !this.profile.apellidos.trim()) {
      this.profileError = 'Nombres y apellidos son obligatorios.';
      return;
    }

    this.savingProfile = true;
    const request: UserProfileRequest = {
      nombres: this.profile.nombres.trim(),
      apellidos: this.profile.apellidos.trim(),
      telefono: this.profile.telefono?.trim() || null,
      direccion: this.profile.direccion?.trim() || null
    };

    this.userService.updateMyProfile(request).subscribe({
      next: user => {
        this.setProfile(user);
        form.form.markAsPristine();
        this.profileSuccess = 'Tus datos se guardaron correctamente.';
        this.savingProfile = false;
      },
      error: (error: HttpErrorResponse) => {
        this.profileError = this.getErrorDetail(error)
          || 'No se pudieron guardar tus datos. Inténtalo de nuevo.';
        this.savingProfile = false;
      }
    });
  }

  changePassword(): void {
    if (this.savingPassword || this.password.contrasenaNueva !== this.confirmPassword) {
      if (this.password.contrasenaNueva !== this.confirmPassword) {
        this.passwordError = 'Las contraseñas nuevas no coinciden.';
      }
      return;
    }

    this.passwordError = '';
    this.passwordSuccess = '';
    this.savingPassword = true;
    this.authService.changePassword(this.password).subscribe({
      next: () => {
        this.password = { contrasenaActual: '', contrasenaNueva: '' };
        this.confirmPassword = '';
        this.passwordSuccess = 'Tu contraseña se actualizó correctamente.';
        this.savingPassword = false;
      },
      error: (error: HttpErrorResponse) => {
        this.passwordError = this.getErrorDetail(error)
          || 'No se pudo actualizar la contraseña. Inténtalo de nuevo.';
        this.savingPassword = false;
      }
    });
  }

  logout(): void {
    if (this.loggingOut) {
      return;
    }

    this.logoutError = '';
    this.loggingOut = true;
    this.authService.logout().subscribe({
      next: () => this.router.navigate(['/login']),
      error: () => {
        this.logoutError = 'La sesión se cerró en este dispositivo, pero el servidor no respondió.';
        this.loggingOut = false;
      }
    });
  }

  get initials(): string {
    if (!this.user) {
      return '';
    }
    return `${this.user.nombres.charAt(0)}${this.user.apellidos.charAt(0)}`.toUpperCase();
  }

  private setProfile(user: IUser): void {
    this.user = user;
    this.profile = {
      nombres: user.nombres,
      apellidos: user.apellidos,
      telefono: user.telefono ?? '',
      direccion: user.direccion ?? ''
    };
    this.localStorage.setUser(user);
  }

  private getErrorDetail(error: HttpErrorResponse): string {
    const detail: unknown = error.error?.detail;
    return typeof detail === 'string' && detail.trim() ? detail : '';
  }
}
