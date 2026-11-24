import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../services/auth-service';
import { CarritoService } from '../../services/carrito-service';
import { Icono } from '../icono/icono';

@Component({
  selector: 'app-navbar',
  imports: [RouterLink, RouterLinkActive, Icono],
  templateUrl: './navbar.html',
  styleUrl: './navbar.scss',
  host: { '(document:click)': 'cerrarMenu()', '(document:keydown.escape)': 'cerrarMenu(); cerrarNav()' },
})
export class Navbar {
  protected readonly auth = inject(AuthService);
  protected readonly carrito = inject(CarritoService);
  private readonly router = inject(Router);

  protected readonly menuAbierto = signal(false);
  protected readonly navAbierta = signal(false);

  protected alternarMenu(evento: Event): void {
    evento.stopPropagation();
    this.menuAbierto.update((abierto) => !abierto);
  }

  protected cerrarMenu(): void {
    this.menuAbierto.set(false);
  }

  protected alternarNav(): void {
    this.navAbierta.update((abierta) => !abierta);
  }

  protected cerrarNav(): void {
    this.navAbierta.set(false);
  }

  protected salir(): void {
    this.auth.cerrarSesion();
    this.menuAbierto.set(false);
    this.navAbierta.set(false);
    void this.router.navigateByUrl('/');
  }
}
