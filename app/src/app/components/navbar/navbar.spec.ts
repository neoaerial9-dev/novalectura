import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { environment } from '../../../environments/environment';
import { iniciarSesion, usuarioPrueba } from '../../testing/ayudas';
import { Navbar } from './navbar';

function crear(sesion: 'ninguna' | 'cliente' | 'admin') {
  localStorage.clear();
  if (sesion === 'cliente') {
    iniciarSesion();
  } else if (sesion === 'admin') {
    iniciarSesion({ ...usuarioPrueba, rol: 'ADMINISTRADOR' });
  }
  TestBed.configureTestingModule({
    imports: [Navbar],
    providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
  });
  const fixture = TestBed.createComponent(Navbar);
  fixture.detectChanges();
  return { fixture, el: fixture.nativeElement as HTMLElement, http: TestBed.inject(HttpTestingController) };
}

describe('Navbar', () => {
  it('un visitante ve el botón Ingresar y no ve enlaces de administración', () => {
    const { el } = crear('ninguna');

    expect(el.textContent).toContain('Ingresar');
    expect(el.textContent).not.toContain('Usuarios');
    expect(el.querySelector('.avatar')).toBeNull();
  });

  it('un cliente ve sus iniciales y el contador del carrito', async () => {
    const { fixture, el, http } = crear('cliente');
    http.match(`${environment.apiUrl}/carrito`).forEach((r) =>
      r.flush({ items: [{ libroId: 1, titulo: 'A', imagen: null, precioUnitario: 5, cantidad: 3, subtotal: 15, stockDisponible: 9, disponible: true }], total: 15 }),
    );
    fixture.detectChanges();
    await fixture.whenStable();

    expect(el.querySelector('.avatar')?.textContent?.trim()).toBe('AP');
    expect(el.querySelector('.carrito__cantidad')?.textContent).toBe('3');
    expect(el.textContent).not.toContain('Usuarios');
  });

  it('un administrador ve Usuarios, Pedidos y Categorías', () => {
    const { el, http } = crear('admin');
    http.match(`${environment.apiUrl}/carrito`).forEach((r) => r.flush({ items: [], total: 0 }));

    expect(el.textContent).toContain('Usuarios');
    expect(el.textContent).toContain('Pedidos');
    expect(el.textContent).toContain('Categorías');
  });

  it('el menú de usuario se abre, ofrece cerrar sesión y la cierra', async () => {
    const { fixture, el, http } = crear('cliente');
    http.match(`${environment.apiUrl}/carrito`).forEach((r) => r.flush({ items: [], total: 0 }));

    el.querySelector<HTMLButtonElement>('.avatar')!.click();
    await fixture.whenStable();
    expect(el.querySelector('[role="menu"]')?.textContent).toContain('Mi perfil');

    el.querySelector<HTMLButtonElement>('.menu__salir')!.click();
    http.expectOne(`${environment.apiUrl}/auth/logout`).flush(null, { status: 204, statusText: 'No Content' });
    await fixture.whenStable();

    expect(el.querySelector('.avatar')).toBeNull();
    expect(el.textContent).toContain('Ingresar');
  });

  it('el botón de menú abre y cierra la navegación en pantallas pequeñas', async () => {
    const { fixture, el } = crear('ninguna');
    const boton = el.querySelector<HTMLButtonElement>('.nav__toggle')!;
    const nav = el.querySelector('#menu-principal')!;

    expect(boton.getAttribute('aria-expanded')).toBe('false');
    expect(nav.classList.contains('abierta')).toBe(false);

    boton.click();
    await fixture.whenStable();
    expect(boton.getAttribute('aria-expanded')).toBe('true');
    expect(nav.classList.contains('abierta')).toBe(true);

    boton.click();
    await fixture.whenStable();
    expect(nav.classList.contains('abierta')).toBe(false);
  });
});
