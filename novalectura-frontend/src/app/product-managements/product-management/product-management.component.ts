import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import {
  BookCategory,
  BookCondition,
  BookPage,
  BookRequest,
  ManagedBook
} from '../../interfaces/books/book-api.interface';
import { BookManagementService } from '../../services/book-management.service';

@Component({
  selector: 'app-product-management',
  standalone: false,
  templateUrl: './product-management.component.html',
  styleUrls: ['./product-management.component.scss']
})
export class ProductManagementComponent implements OnInit {
  readonly conditions: { value: BookCondition; label: string }[] = [
    { value: 'COMO_NUEVO', label: 'Como nuevo' },
    { value: 'MUY_BUENO', label: 'Muy bueno' },
    { value: 'BUENO', label: 'Bueno' },
    { value: 'ACEPTABLE', label: 'Aceptable' }
  ];

  books: ManagedBook[] = [];
  categories: BookCategory[] = [];
  currentPage = 0;
  totalPages = 0;
  totalBooks = 0;
  loadingBooks = false;
  savingBook = false;
  savingCategory = false;
  showBookEditor = false;
  editingBookId: number | null = null;
  searchText = '';
  errorMessage = '';
  successMessage = '';
  categoryError = '';
  categoryName = '';
  categoryDescription = '';
  bookForm: BookRequest = this.emptyBookForm();

  constructor(private bookService: BookManagementService) { }

  ngOnInit(): void {
    this.loadCategories();
    this.loadBooks(0);
  }

  loadBooks(page: number): void {
    this.loadingBooks = true;
    this.errorMessage = '';
    const search = this.searchText.trim();
    const request = search
      ? this.bookService.getCatalogBooks(page, search, null, '')
      : this.bookService.getBooks(page);

    request.subscribe({
      next: (result: BookPage) => {
        this.books = result.contenido;
        this.currentPage = result.pagina;
        this.totalPages = result.totalPaginas;
        this.totalBooks = result.totalElementos;
        this.loadingBooks = false;
      },
      error: (error: HttpErrorResponse) => {
        this.errorMessage = this.errorDetail(error, 'No se pudieron cargar los libros.');
        this.loadingBooks = false;
      }
    });
  }

  searchBooks(): void {
    this.loadBooks(0);
  }

  addBook(): void {
    this.resetBookForm();
    this.errorMessage = '';
    this.successMessage = '';
    this.showBookEditor = true;
  }

  loadCategories(): void {
    this.bookService.getCategories().subscribe({
      next: categories => {
        this.categories = categories;
        if (this.bookForm.idCategoria === 0 && categories.length > 0) {
          this.bookForm.idCategoria = categories[0].idCategoria;
        }
      },
      error: (error: HttpErrorResponse) => {
        this.errorMessage = this.errorDetail(error, 'No se pudieron cargar las categorías.');
      }
    });
  }

  saveBook(): void {
    if (this.savingBook) {
      return;
    }

    this.savingBook = true;
    this.errorMessage = '';
    this.successMessage = '';
    const request: BookRequest = {
      ...this.bookForm,
      titulo: this.bookForm.titulo.trim(),
      autor: this.bookForm.autor.trim(),
      descripcion: this.bookForm.descripcion.trim(),
      imagen: this.bookForm.imagen.trim()
    };
    const creatingBook = this.editingBookId === null;
    const operation = creatingBook
      ? this.bookService.createBook(request)
      : this.bookService.updateBook(this.editingBookId, request);

    operation.subscribe({
      next: () => {
        this.successMessage = creatingBook
          ? 'Libro creado correctamente.'
          : 'Libro actualizado correctamente.';
        this.resetBookForm();
        this.showBookEditor = false;
        this.savingBook = false;
        this.loadBooks(creatingBook ? 0 : this.currentPage);
      },
      error: (error: HttpErrorResponse) => {
        this.errorMessage = this.errorDetail(error, 'No se pudo guardar el libro.');
        this.savingBook = false;
      }
    });
  }

  editBook(book: ManagedBook): void {
    this.editingBookId = book.idLibro;
    this.bookForm = {
      titulo: book.titulo,
      autor: book.autor,
      precio: book.precio,
      stock: book.stock,
      descripcion: book.descripcion ?? '',
      imagen: book.imagen ?? '',
      condicion: book.condicion,
      idCategoria: book.categoria.idCategoria
    };
    this.errorMessage = '';
    this.successMessage = '';
    this.showBookEditor = true;
  }

  deleteBook(book: ManagedBook): void {
    if (!window.confirm(`¿Eliminar "${book.titulo}" del catálogo?`)) {
      return;
    }

    this.errorMessage = '';
    this.successMessage = '';
    this.bookService.deleteBook(book.idLibro).subscribe({
      next: () => {
        this.successMessage = 'Libro eliminado del catálogo.';
        if (this.books.length === 1 && this.currentPage > 0) {
          this.loadBooks(this.currentPage - 1);
        } else {
          this.loadBooks(this.currentPage);
        }
      },
      error: (error: HttpErrorResponse) => {
        this.errorMessage = this.errorDetail(error, 'No se pudo eliminar el libro.');
      }
    });
  }

  cancelEdit(): void {
    this.resetBookForm();
    this.showBookEditor = false;
    this.errorMessage = '';
    this.successMessage = '';
  }

  createCategory(): void {
    const nombre = this.categoryName.trim();
    if (!nombre || this.savingCategory) {
      return;
    }

    this.savingCategory = true;
    this.categoryError = '';
    this.bookService.createCategory(nombre, this.categoryDescription.trim()).subscribe({
      next: category => {
        this.categories = [...this.categories, category].sort((a, b) =>
          a.nombre.localeCompare(b.nombre));
        this.bookForm.idCategoria = category.idCategoria;
        this.categoryName = '';
        this.categoryDescription = '';
        this.savingCategory = false;
      },
      error: (error: HttpErrorResponse) => {
        this.categoryError = this.errorDetail(error, 'No se pudo crear la categoría.');
        this.savingCategory = false;
      }
    });
  }

  private resetBookForm(): void {
    this.editingBookId = null;
    this.bookForm = this.emptyBookForm();
    if (this.categories.length > 0) {
      this.bookForm.idCategoria = this.categories[0].idCategoria;
    }
  }

  private emptyBookForm(): BookRequest {
    return {
      titulo: '',
      autor: '',
      precio: 0.01,
      stock: 0,
      descripcion: '',
      imagen: '',
      condicion: 'COMO_NUEVO',
      idCategoria: 0
    };
  }

  private errorDetail(error: HttpErrorResponse, fallback: string): string {
    const detail: unknown = error.error?.detail;
    return typeof detail === 'string' && detail.trim() ? detail : fallback;
  }
}
