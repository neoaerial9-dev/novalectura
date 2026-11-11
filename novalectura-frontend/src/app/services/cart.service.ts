import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { environment } from '../../enviroment/enviroment';
import {
  CartItemRequest,
  CartResponse
} from '../interfaces/books/book-api.interface';

@Injectable({ providedIn: 'root' })
export class CartService {
  private readonly cartUrl = `${environment.apiUrl}/cart`;
  private readonly cartSubject = new BehaviorSubject<CartResponse | null>(null);
  readonly cart$ = this.cartSubject.asObservable();

  constructor(private http: HttpClient) { }

  getCart(): Observable<CartResponse> {
    return this.http.get<CartResponse>(this.cartUrl).pipe(
      tap(cart => this.cartSubject.next(cart))
    );
  }

  addItem(request: CartItemRequest): Observable<CartResponse> {
    return this.http.post<CartResponse>(`${this.cartUrl}/items`, request).pipe(
      tap(cart => this.cartSubject.next(cart))
    );
  }

  updateQuantity(idDetail: number, quantity: number): Observable<CartResponse> {
    return this.http.put<CartResponse>(`${this.cartUrl}/items/${idDetail}`, {
      cantidad: quantity
    }).pipe(
      tap(cart => this.cartSubject.next(cart))
    );
  }

  removeItem(idDetail: number): Observable<CartResponse> {
    return this.http.delete<CartResponse>(`${this.cartUrl}/items/${idDetail}`).pipe(
      tap(cart => this.cartSubject.next(cart))
    );
  }

}
