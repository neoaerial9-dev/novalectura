import { Component, OnInit } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { CartDetail, CartResponse } from '../../interfaces/books/book-api.interface';
import { CartService } from '../../services/cart.service';

@Component({
  selector: 'app-shoppings',
  standalone: false,
  templateUrl: './shoppings.component.html',
  styleUrls: ['./shoppings.component.css']
})
export class ShoppingsComponent implements OnInit {
  cart: CartResponse | null = null;
  loading = true;
  busyDetailId: number | null = null;
  errorMessage = '';

  constructor(private cartService: CartService) { }

  ngOnInit(): void {
    this.loadCart();
  }

  loadCart(): void {
    this.loading = true;
    this.errorMessage = '';
    this.cartService.getCart().subscribe({
      next: cart => {
        this.cart = cart;
        this.loading = false;
      },
      error: () => {
        this.errorMessage = 'No pudimos cargar tu carrito. Inténtalo de nuevo.';
        this.loading = false;
      }
    });
  }

  changeQuantity(item: CartDetail, quantity: number): void {
    if (this.busyDetailId !== null || quantity < 1 || quantity > item.stockDisponible) {
      return;
    }

    this.busyDetailId = item.idDetalle;
    this.errorMessage = '';
    this.cartService.updateQuantity(item.idDetalle, quantity).subscribe({
      next: cart => {
        this.cart = cart;
        this.busyDetailId = null;
      },
      error: (error: HttpErrorResponse) => {
        this.errorMessage = error.status === 409
          ? 'El stock disponible cambió. Actualizamos tu carrito; revisa la cantidad e inténtalo de nuevo.'
          : 'No pudimos actualizar la cantidad. Inténtalo de nuevo.';
        this.busyDetailId = null;
        if (error.status === 409) {
          this.cartService.getCart().subscribe({
            next: cart => this.cart = cart,
            error: () => this.errorMessage = 'El stock cambió y no pudimos actualizar el carrito. Recarga la página para ver las cantidades disponibles.'
          });
        }
      }
    });
  }

  removeItem(item: CartDetail): void {
    if (this.busyDetailId !== null) {
      return;
    }

    this.busyDetailId = item.idDetalle;
    this.errorMessage = '';
    this.cartService.removeItem(item.idDetalle).subscribe({
      next: cart => {
        this.cart = cart;
        this.busyDetailId = null;
      },
      error: () => {
        this.errorMessage = 'No pudimos quitar el libro del carrito. Inténtalo de nuevo.';
        this.busyDetailId = null;
      }
    });
  }

  get itemCount(): number {
    return this.cart?.detalles.reduce((count, item) => count + item.cantidad, 0) ?? 0;
  }
}
