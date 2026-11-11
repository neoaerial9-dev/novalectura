import { Component, EventEmitter, HostListener, Input, Output } from '@angular/core';
import { ManagedBook } from '../../interfaces/books/book-api.interface';

@Component({
  selector: 'app-book-detail',
  standalone: false,
  templateUrl: './book-detail.component.html',
  styleUrls: ['./book-detail.component.css']
})
export class BookDetailComponent {
  @Input() book!: ManagedBook;
  @Input() adding = false;
  @Input() isOwnBook = false;
  @Input() message = '';
  @Output() close = new EventEmitter<void>();
  @Output() addToCart = new EventEmitter<{ book: ManagedBook; quantity: number }>();

  quantity = 1;

  get total(): number {
    return this.book.precio * this.quantity;
  }

  get cannotAdd(): boolean {
    return this.adding || this.book.stock < 1 || this.isOwnBook;
  }

  decreaseQuantity(): void {
    if (this.quantity > 1) {
      this.quantity--;
    }
  }

  increaseQuantity(): void {
    if (this.quantity < this.book.stock) {
      this.quantity++;
    }
  }

  confirmAdd(): void {
    if (!this.cannotAdd) {
      this.addToCart.emit({ book: this.book, quantity: this.quantity });
    }
  }

  onBackdropClick(event: MouseEvent): void {
    if (event.target === event.currentTarget) {
      this.close.emit();
    }
  }

  @HostListener('document:keydown.escape')
  onEscape(): void {
    this.close.emit();
  }
}
