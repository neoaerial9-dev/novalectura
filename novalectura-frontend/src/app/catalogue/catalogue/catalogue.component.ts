import { Component, OnInit } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';
import { BookCategory, BookPage, ManagedBook } from '../../interfaces/books/book-api.interface';
import { BookManagementService } from '../../services/book-management.service';
import { CartService } from '../../services/cart.service';
import { LocalStorageService } from '../../services/local-storage.service';

@Component({
  selector: 'app-catalogue',
  standalone: false,
  templateUrl: './catalogue.component.html',
  styleUrls: ['./catalogue.component.css']
})
export class CatalogueComponent implements OnInit {
  books: ManagedBook[] = [];
  categories: BookCategory[] = [];
  search = '';
  selectedCategory: number | null = null;
  order = '';
  page = 0;
  totalPages = 0;
  totalBooks = 0;
  loading = true;
  errorMessage = '';
  categoriesErrorMessage = '';
  cartMessage = '';
  addingBookId: number | null = null;
  selectedBook: ManagedBook | null = null;

  constructor(
    private bookService: BookManagementService,
    private cartService: CartService,
    private localStorage: LocalStorageService,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.loadCategories();
    this.loadBooks();
  }

  loadBooks(page = 0): void {
    this.loading = true;
    this.errorMessage = '';
    this.page = page;

    this.bookService.getCatalogBooks(
      page,
      this.search,
      this.selectedCategory,
      this.order
    ).subscribe({
      next: (result: BookPage) => {
        this.books = result.contenido;
        this.totalBooks = result.totalElementos;
        this.totalPages = result.totalPaginas;
        this.page = result.pagina;
        this.loading = false;
      },
      error: () => {
        this.errorMessage = 'No pudimos cargar el catálogo. Inténtalo de nuevo.';
        this.loading = false;
      }
    });
  }

  loadCategories(): void {
    this.bookService.getCategories().subscribe({
      next: categories => this.categories = categories,
      error: () => this.categoriesErrorMessage = 'No pudimos cargar las categorías.'
    });
  }

  openBookDetails(book: ManagedBook): void {
    this.cartMessage = '';
    this.selectedBook = book;
  }

  closeBookDetails(): void {
    this.selectedBook = null;
  }

  isOwnBook(book: ManagedBook | null): boolean {
    const user = this.localStorage.getUser();
    return book !== null && user !== null && user.idUsuario === book.vendedor.idUsuario;
  }

  addToCart(selection: { book: ManagedBook; quantity: number }): void {
    const { book, quantity } = selection;
    this.cartMessage = '';
    const user = this.localStorage.getUser();

    if (!this.localStorage.getToken() || !user) {
      this.router.navigate(['/login'], { queryParams: { returnUrl: '/catalogo' } });
      return;
    }

    if (user.idUsuario === book.vendedor.idUsuario) {
      this.cartMessage = 'No puedes agregar al carrito un libro publicado por ti.';
      return;
    }

    this.addingBookId = book.idLibro;
    this.cartService.addItem({ idLibro: book.idLibro, cantidad: quantity }).subscribe({
      next: () => {
        this.cartMessage = `Se agregaron ${quantity} ${quantity === 1 ? 'ejemplar' : 'ejemplares'} de "${book.titulo}" a tu carrito.`;
        this.addingBookId = null;
        this.closeBookDetails();
      },
      error: (error: HttpErrorResponse) => {
        this.cartMessage = error.status === 401 || error.status === 403
          ? 'Inicia sesión para agregar libros al carrito.'
          : error.status === 409
            ? 'No hay suficiente stock disponible para esa cantidad.'
          : 'No se pudo agregar el libro al carrito. Inténtalo de nuevo.';
        this.addingBookId = null;
      }
    });
  }

  previousPage(): void {
    if (this.page > 0) {
      this.loadBooks(this.page - 1);
    }
  }

  nextPage(): void {
    if (this.page + 1 < this.totalPages) {
      this.loadBooks(this.page + 1);
    }
  }
}
