import { Component, OnInit } from '@angular/core';
import { Observable, map } from 'rxjs';
import { CartService } from '../../services/cart.service';
import { LocalStorageService } from '../../services/local-storage.service';
import { Router } from '@angular/router';
import { IUser } from '../../interfaces/user.interface';

@Component({
  selector: 'app-top-menu',
  standalone: false,
  templateUrl: './top-menu.component.html',
  styleUrls: ['./top-menu.component.css']
})
export class TopMenuComponent implements OnInit {

  cartCount$: Observable<number>;
  isAuthenticated = false;
  user: IUser | null = null;
  cartLoadError = '';

  constructor(
    private cartService: CartService,
    private localStorage: LocalStorageService,
    private router: Router
  ) {
    this.cartCount$ = this.cartService.cart$.pipe(
      map(cart => cart?.detalles.reduce((count, item) => count + item.cantidad, 0) ?? 0)
    );
  }

  ngOnInit(): void {
    this.user = this.localStorage.getUser();
    this.isAuthenticated = !!this.localStorage.getToken() && !!this.user;
    if (this.isAuthenticated) {
      this.cartService.getCart().subscribe({
        error: () => this.cartLoadError = 'No se pudo actualizar el contador del carrito.'
      });
    }
  }

  get initials(): string {
    if (!this.user) {
      return '';
    }
    return `${this.user.nombres.charAt(0)}${this.user.apellidos.charAt(0)}`.toUpperCase();
  }

  openCart(): void {
    if (!this.isAuthenticated) {
      this.router.navigate(['/login'], { queryParams: { returnUrl: '/carrito' } });
      return;
    }
    this.router.navigate(['/carrito']);
  }

}