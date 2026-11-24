import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../environments/environment';
import { AuthResponse, Usuario } from '../core/models';
import { AuthService } from './auth-service';

const usuario: Usuario = {
  id: 1, nombres: 'Ana', apellidos: 'Paz', correo: 'ana@mail.com', rol: 'CLIENTE',
  telefono: null, direccionEnvio: null, activo: true, fechaRegistro: '2026-11-14T10:00:00',
};
const respuesta: AuthResponse = { token: 'TOKEN', tipo: 'Bearer', usuario };

function crear(): { auth: AuthService; http: HttpTestingController } {
  TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
  return { auth: TestBed.inject(AuthService), http: TestBed.inject(HttpTestingController) };
}

describe('AuthService', () => {
  beforeEach(() => localStorage.clear());

  it('comienza sin sesión', () => {
    const { auth } = crear();
    expect(auth.autenticado()).toBe(false);
    expect(auth.token()).toBeNull();
  });

  it('login guarda token y usuario y los persiste', () => {
    const { auth, http } = crear();

    auth.login({ correo: 'ana@mail.com', contrasena: 'secreto123' }).subscribe();
    const req = http.expectOne(`${environment.apiUrl}/auth/login`);
    expect(req.request.method).toBe('POST');
    req.flush(respuesta);

    expect(auth.autenticado()).toBe(true);
    expect(auth.usuario()?.correo).toBe('ana@mail.com');
    expect(auth.iniciales()).toBe('AP');
    expect(localStorage.getItem('nl.token')).toBe('TOKEN');
  });

  it('restaura la sesión guardada al crearse', () => {
    localStorage.setItem('nl.token', 'TOKEN');
    localStorage.setItem('nl.usuario', JSON.stringify(usuario));

    const { auth } = crear();

    expect(auth.autenticado()).toBe(true);
    expect(auth.token()).toBe('TOKEN');
  });

  it('ignora un usuario guardado con JSON dañado', () => {
    localStorage.setItem('nl.token', 'TOKEN');
    localStorage.setItem('nl.usuario', '{no-es-json');

    const { auth } = crear();

    expect(auth.autenticado()).toBe(false);
  });

  it('esAdmin refleja el rol', () => {
    const { auth, http } = crear();
    auth.login({ correo: 'a@b.com', contrasena: 'x' }).subscribe();
    http.expectOne(`${environment.apiUrl}/auth/login`).flush({ ...respuesta, usuario: { ...usuario, rol: 'ADMINISTRADOR' } });

    expect(auth.esAdmin()).toBe(true);
  });

  it('cerrarSesion avisa a la API y limpia todo', () => {
    localStorage.setItem('nl.token', 'TOKEN');
    localStorage.setItem('nl.usuario', JSON.stringify(usuario));
    const { auth, http } = crear();

    auth.cerrarSesion();

    http.expectOne(`${environment.apiUrl}/auth/logout`).flush(null, { status: 204, statusText: 'No Content' });
    expect(auth.autenticado()).toBe(false);
    expect(localStorage.getItem('nl.token')).toBeNull();
    expect(localStorage.getItem('nl.usuario')).toBeNull();
  });

  it('limpiar no llama a la API', () => {
    localStorage.setItem('nl.token', 'TOKEN');
    localStorage.setItem('nl.usuario', JSON.stringify(usuario));
    const { auth, http } = crear();

    auth.limpiar();

    http.expectNone(`${environment.apiUrl}/auth/logout`);
    expect(auth.autenticado()).toBe(false);
  });

  it('actualizarUsuario cambia los datos y los persiste', () => {
    const { auth, http } = crear();
    auth.login({ correo: 'a@b.com', contrasena: 'x' }).subscribe();
    http.expectOne(`${environment.apiUrl}/auth/login`).flush(respuesta);

    auth.actualizarUsuario({ ...usuario, nombres: 'María' });

    expect(auth.usuario()?.nombres).toBe('María');
    expect(JSON.parse(localStorage.getItem('nl.usuario') ?? '{}').nombres).toBe('María');
  });
});
