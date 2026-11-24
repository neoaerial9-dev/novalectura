import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { environment } from '../../environments/environment';
import { AuthService } from '../services/auth-service';
import { authInterceptor } from './auth-interceptor';
import { errorInterceptor } from './error-interceptor';

const usuario = { id: 1, nombres: 'Ana', apellidos: 'Paz', correo: 'ana@mail.com', rol: 'CLIENTE' };

function configurar(conSesion: boolean): { http: HttpClient; ctrl: HttpTestingController; router: Router } {
  localStorage.clear();
  if (conSesion) {
    localStorage.setItem('nl.token', 'TOKEN');
    localStorage.setItem('nl.usuario', JSON.stringify(usuario));
  }
  TestBed.configureTestingModule({
    providers: [
      provideRouter([]),
      provideHttpClient(withInterceptors([authInterceptor, errorInterceptor])),
      provideHttpClientTesting(),
    ],
  });
  return { http: TestBed.inject(HttpClient), ctrl: TestBed.inject(HttpTestingController), router: TestBed.inject(Router) };
}

describe('authInterceptor', () => {
  it('agrega el Bearer a las peticiones de la API', () => {
    const { http, ctrl } = configurar(true);

    http.get(`${environment.apiUrl}/carrito`).subscribe();

    expect(ctrl.expectOne(`${environment.apiUrl}/carrito`).request.headers.get('Authorization')).toBe('Bearer TOKEN');
  });

  it('no envía el token a otros dominios', () => {
    const { http, ctrl } = configurar(true);

    http.get('https://otro-sitio.com/datos').subscribe();

    expect(ctrl.expectOne('https://otro-sitio.com/datos').request.headers.has('Authorization')).toBe(false);
  });

  it('no agrega cabecera si no hay sesión', () => {
    const { http, ctrl } = configurar(false);

    http.get(`${environment.apiUrl}/libros`).subscribe();

    expect(ctrl.expectOne(`${environment.apiUrl}/libros`).request.headers.has('Authorization')).toBe(false);
  });
});

describe('errorInterceptor', () => {
  it('ante un 401 con sesión activa la limpia y lleva a /ingresar', () => {
    const { http, ctrl, router } = configurar(true);
    const navegar = vi.spyOn(router, 'navigate').mockResolvedValue(true);

    http.get(`${environment.apiUrl}/carrito`).subscribe({ error: () => undefined });
    ctrl.expectOne(`${environment.apiUrl}/carrito`).flush(null, { status: 401, statusText: 'Unauthorized' });

    expect(TestBed.inject(AuthService).autenticado()).toBe(false);
    expect(navegar).toHaveBeenCalledWith(['/ingresar'], expect.objectContaining({ queryParams: expect.objectContaining({ expirada: 1 }) }));
  });

  it('un 401 sin sesión (credenciales incorrectas) no redirige', () => {
    const { http, ctrl, router } = configurar(false);
    const navegar = vi.spyOn(router, 'navigate').mockResolvedValue(true);
    let fallo = false;

    http.post(`${environment.apiUrl}/auth/login`, {}).subscribe({ error: () => (fallo = true) });
    ctrl.expectOne(`${environment.apiUrl}/auth/login`).flush(null, { status: 401, statusText: 'Unauthorized' });

    expect(fallo).toBe(true);
    expect(navegar).not.toHaveBeenCalled();
  });

  it('otros errores se propagan sin tocar la sesión', () => {
    const { http, ctrl } = configurar(true);

    http.get(`${environment.apiUrl}/libros/9`).subscribe({ error: () => undefined });
    ctrl.expectOne(`${environment.apiUrl}/libros/9`).flush(null, { status: 404, statusText: 'Not Found' });

    expect(TestBed.inject(AuthService).autenticado()).toBe(true);
  });
});
