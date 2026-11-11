import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../enviroment/enviroment';
import {
  BookCategory,
  BookPage,
  BookRequest,
  ManagedBook
} from '../interfaces/books/book-api.interface';

@Injectable({ providedIn: 'root' })
export class BookManagementService {
  private readonly booksUrl = `${environment.apiUrl}/books`;
  private readonly categoriesUrl = `${environment.apiUrl}/categories`;

  constructor(private http: HttpClient) { }

  getBooks(page: number): Observable<BookPage> {
    const params = new HttpParams().set('pagina', page).set('tamano', 12);
    return this.http.get<BookPage>(this.booksUrl, { params });
  }

  getCatalogBooks(
    page: number,
    search: string,
    categoryId: number | null,
    order: string
  ): Observable<BookPage> {
    let params = new HttpParams()
      .set('pagina', page)
      .set('tamano', 12)
      .set('orden', order);

    if (search.trim()) {
      params = params.set('q', search.trim());
    }
    if (categoryId !== null) {
      params = params.set('idCategoria', categoryId);
    }

    return this.http.get<BookPage>(`${this.booksUrl}/search`, { params });
  }

  getCategories(): Observable<BookCategory[]> {
    return this.http.get<BookCategory[]>(this.categoriesUrl);
  }

  createCategory(nombre: string, descripcion: string): Observable<BookCategory> {
    return this.http.post<BookCategory>(this.categoriesUrl, { nombre, descripcion });
  }

  createBook(request: BookRequest): Observable<ManagedBook> {
    return this.http.post<ManagedBook>(this.booksUrl, request);
  }

  updateBook(id: number, request: BookRequest): Observable<ManagedBook> {
    return this.http.put<ManagedBook>(`${this.booksUrl}/${id}`, request);
  }

  deleteBook(id: number): Observable<void> {
    return this.http.delete<void>(`${this.booksUrl}/${id}`);
  }
}
