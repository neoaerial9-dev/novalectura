import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { environment } from '../../../environments/environment';
import { enviarFormulario, escribir, usuarioPrueba } from '../../testing/ayudas';
import { Login } from './login';

function crear() {
  localStorage.clear();
  TestBed.configureTestingModule({
    imports: [Login],
    providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
  });
  const fixture = TestBed.createComponent(Login);
  fixture.detectChanges();
  return {
    fixture,
    el: fixture.nativeElement as HTMLElement,
    http: TestBed.inject(HttpTestingController),
    router: TestBed.inject(Router),
  };
}

describe('Login', () => {
  it('no llama a la API si el formulario está vacío y muestra los errores', async () => {
    const { fixture, el, http } = crear();

    enviarFormulario(fixture);
    await fixture.whenStable();

    http.expectNone(`${environment.apiUrl}/auth/login`);
    expect(el.textContent).toContain('Ingresa un correo válido');
    expect(el.textContent).toContain('Ingresa tu contraseña');
  });

  it('inicia sesión y navega a la página de destino', async () => {
    const { fixture, http, router } = crear();
    const navegar = vi.spyOn(router, 'navigateByUrl').mockResolvedValue(true);
    fixture.componentRef.setInput('redirect', '/carrito');
    escribir(fixture, '#correo', 'ana@mail.com');
    escribir(fixture, '#contrasena', 'secreto123');

    enviarFormulario(fixture);
    const req = http.expectOne(`${environment.apiUrl}/auth/login`);
    expect(req.request.body).toEqual({ correo: 'ana@mail.com', contrasena: 'secreto123' });
    req.flush({ token: 'T', tipo: 'Bearer', usuario: usuarioPrueba });

    expect(navegar).toHaveBeenCalledWith('/carrito');
  });

  it('ignora destinos externos y va al inicio', async () => {
    const { fixture, http, router } = crear();
    const navegar = vi.spyOn(router, 'navigateByUrl').mockResolvedValue(true);
    fixture.componentRef.setInput('redirect', 'https://malo.com');
    escribir(fixture, '#correo', 'ana@mail.com');
    escribir(fixture, '#contrasena', 'x');

    enviarFormulario(fixture);
    http.expectOne(`${environment.apiUrl}/auth/login`).flush({ token: 'T', tipo: 'Bearer', usuario: usuarioPrueba });

    expect(navegar).toHaveBeenCalledWith('/');
  });

  it('muestra el mensaje de la API cuando las credenciales son incorrectas', async () => {
    const { fixture, el, http } = crear();
    escribir(fixture, '#correo', 'ana@mail.com');
    escribir(fixture, '#contrasena', 'mala');

    enviarFormulario(fixture);
    http
      .expectOne(`${environment.apiUrl}/auth/login`)
      .flush({ detail: 'Correo o contraseña incorrectos' }, { status: 401, statusText: 'Unauthorized' });
    await fixture.whenStable();

    expect(el.querySelector('[role="alert"]')?.textContent).toContain('Correo o contraseña incorrectos');
  });

  it('avisa cuando la sesión expiró', async () => {
    const { fixture, el } = crear();
    fixture.componentRef.setInput('expirada', '1');
    await fixture.whenStable();

    expect(el.textContent).toContain('Tu sesión expiró');
  });
});
