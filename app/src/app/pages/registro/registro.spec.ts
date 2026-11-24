import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { environment } from '../../../environments/environment';
import { enviarFormulario, escribir, usuarioPrueba } from '../../testing/ayudas';
import { Registro } from './registro';

function crear() {
  localStorage.clear();
  TestBed.configureTestingModule({
    imports: [Registro],
    providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
  });
  const fixture = TestBed.createComponent(Registro);
  fixture.detectChanges();
  return { fixture, el: fixture.nativeElement as HTMLElement, http: TestBed.inject(HttpTestingController), router: TestBed.inject(Router) };
}

function llenar(fixture: ReturnType<typeof crear>['fixture'], confirmar = 'secreto123'): void {
  escribir(fixture, '#nombres', 'Ana');
  escribir(fixture, '#apellidos', 'Paz');
  escribir(fixture, '#correo', 'ana@mail.com');
  escribir(fixture, '#contrasena', 'secreto123');
  escribir(fixture, '#confirmar', confirmar);
}

describe('Registro', () => {
  it('rechaza contraseñas que no coinciden', async () => {
    const { fixture, el, http } = crear();
    llenar(fixture, 'otra-cosa');

    enviarFormulario(fixture);
    await fixture.whenStable();

    http.expectNone(`${environment.apiUrl}/auth/registro`);
    expect(el.textContent).toContain('Las contraseñas no coinciden');
  });

  it('exige una contraseña de al menos 8 caracteres', async () => {
    const { fixture, el, http } = crear();
    llenar(fixture);
    escribir(fixture, '#contrasena', 'corta');
    escribir(fixture, '#confirmar', 'corta');

    enviarFormulario(fixture);
    await fixture.whenStable();

    http.expectNone(`${environment.apiUrl}/auth/registro`);
    expect(el.textContent).toContain('entre 8 y 64 caracteres');
  });

  it('registra al usuario, inicia sesión y navega', async () => {
    const { fixture, http, router } = crear();
    const navegar = vi.spyOn(router, 'navigateByUrl').mockResolvedValue(true);
    llenar(fixture);

    enviarFormulario(fixture);
    const registro = http.expectOne(`${environment.apiUrl}/auth/registro`);
    expect(registro.request.body).toEqual({ nombres: 'Ana', apellidos: 'Paz', correo: 'ana@mail.com', contrasena: 'secreto123' });
    registro.flush(usuarioPrueba);
    const login = http.expectOne(`${environment.apiUrl}/auth/login`);
    expect(login.request.body).toEqual({ correo: 'ana@mail.com', contrasena: 'secreto123' });
    login.flush({ token: 'T', tipo: 'Bearer', usuario: usuarioPrueba });

    expect(navegar).toHaveBeenCalledWith('/');
  });

  it('muestra el conflicto cuando el correo ya existe', async () => {
    const { fixture, el, http } = crear();
    llenar(fixture);

    enviarFormulario(fixture);
    http
      .expectOne(`${environment.apiUrl}/auth/registro`)
      .flush({ detail: 'El correo ya está registrado' }, { status: 409, statusText: 'Conflict' });
    await fixture.whenStable();

    expect(el.querySelector('[role="alert"]')?.textContent).toContain('El correo ya está registrado');
  });
});
