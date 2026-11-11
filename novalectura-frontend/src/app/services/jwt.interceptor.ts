import { Injectable } from '@angular/core';
import {
  HttpEvent,
  HttpHandler,
  HttpInterceptor as AngularHttpInterceptor,
  HttpRequest
} from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../enviroment/enviroment';
import { LocalStorageService } from './local-storage.service';

@Injectable()
export class JwtInterceptor implements AngularHttpInterceptor {
  constructor(private localStorage: LocalStorageService) { }

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const isApiRequest = request.url === environment.apiUrl
      || request.url.indexOf(`${environment.apiUrl}/`) === 0;
    const token = isApiRequest ? this.localStorage.getToken() : null;

    if (!token) {
      return next.handle(request);
    }

    return next.handle(request.clone({
      setHeaders: { Authorization: `Bearer ${token}` }
    }));
  }
}
