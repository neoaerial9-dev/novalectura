import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { environment } from '../../../environments/environment';
import { LibroService } from '../../services/libro-service';
import { iniciarSesion, libroPrueba, paginaDe } from '../../testing/ayudas';
import { Catalogo } from './catalogo';

function crear(conSesion = false) {
  localStorage.clear();
  if (conSesion) {
    iniciarSesion();
  }
  TestBed.configureTestingModule({
    imports: [Catalogo],
    providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
  });
  const fixture = TestBed.createComponent(Catalogo);
  fixture.detectChanges();
  const http = TestBed.inject(HttpTestingController);
  return { fixture, el: fixture.nativeElement as HTMLElement, http, router: TestBed.inject(Router) };
}

const esLibros = (r: { url: string }) => r.url === `${environment.apiUrl}/libros`;

describe('Catalogo', () => {
  it('carga categorías y libros y muestra una tarjeta por libro', async () => {
    const { fixture, el, http } = crear();

    http.expectOne(`${environment.apiUrl}/categorias`).flush([{ id: 1, nombre: 'Ficción', descripcion: null }]);
    const req = http.expectOne(esLibros);
    expect(req.request.params.get('page')).toBe('0');
    expect(req.request.params.get('size')).toBe('12');
    expect(req.request.params.get('sort')).toBe('id,desc');
    req.flush(paginaDe([libroPrueba(1), libroPrueba(2)]));
    await fixture.whenStable();

    expect(el.querySelectorAll('app-libro-card').length).toBe(2);
    expect(el.textContent).toContain('2 libros encontrados');
    expect(el.textContent).toContain('Libro 1');
  });

  it('muestra la portada de bienvenida solo cuando se indica', async () => {
    const { fixture, el, http } = crear();
    http.match(() => true).forEach((r) => r.flush(r.request.url.endsWith('/categorias') ? [] : paginaDe([])));
    expect(el.querySelector('.hero')).toBeNull();

    fixture.componentRef.setInput('hero', true);
    await fixture.whenStable();

    expect(el.querySelector('.hero')?.textContent).toContain('Descubre tu próxima');
  });

  it('al cambiar la categoría vuelve a consultar con el filtro y desde la página 0', async () => {
    const { fixture, http } = crear();
    http.expectOne(`${environment.apiUrl}/categorias`).flush([{ id: 4, nombre: 'Historia', descripcion: null }]);
    http.expectOne(esLibros).flush(paginaDe([libroPrueba(1)]));
    await fixture.whenStable();

    const select = (fixture.nativeElement as HTMLElement).querySelector('select')!;
    select.value = select.options[1].value;
    select.dispatchEvent(new Event('change'));

    const req = http.expectOne(esLibros);
    expect(req.request.params.get('categoriaId')).toBe('4');
    expect(req.request.params.get('page')).toBe('0');
    req.flush(paginaDe([]));
    await fixture.whenStable();

    expect((fixture.nativeElement as HTMLElement).textContent).toContain('No encontramos libros');
  });

  it('muestra el error si la API falla', async () => {
    const { fixture, el, http } = crear();
    http.expectOne(`${environment.apiUrl}/categorias`).flush([]);
    http.expectOne(esLibros).flush({ detail: 'Servicio caído' }, { status: 500, statusText: 'Server Error' });
    await fixture.whenStable();

    expect(el.querySelector('[role="alert"]')?.textContent).toContain('Servicio caído');
  });

  it('un visitante que agrega al carrito es enviado a iniciar sesión', async () => {
    const { fixture, el, http, router } = crear();
    const navegar = vi.spyOn(router, 'navigate').mockResolvedValue(true);
    http.expectOne(`${environment.apiUrl}/categorias`).flush([]);
    http.expectOne(esLibros).flush(paginaDe([libroPrueba(1)]));
    await fixture.whenStable();

    el.querySelector<HTMLButtonElement>('.card__pie button')!.click();

    expect(navegar).toHaveBeenCalledWith(['/ingresar'], expect.objectContaining({ queryParams: expect.anything() }));
    http.expectNone(`${environment.apiUrl}/carrito/items`);
  });

  it('un usuario con sesión agrega el libro al carrito', async () => {
    const { fixture, el, http } = crear(true);
    http.match(`${environment.apiUrl}/carrito`).forEach((r) => r.flush({ items: [], total: 0 }));
    http.expectOne(`${environment.apiUrl}/categorias`).flush([]);
    http.expectOne(esLibros).flush(paginaDe([libroPrueba(1)]));
    await fixture.whenStable();

    el.querySelector<HTMLButtonElement>('.card__pie button')!.click();

    const req = http.expectOne(`${environment.apiUrl}/carrito/items`);
    expect(req.request.body).toEqual({ libroId: 1, cantidad: 1 });
    req.flush({ items: [], total: 0 });
  });

  it('abre el detalle del libro al pulsar la portada y lo cierra', async () => {
    const { fixture, el, http } = crear();
    http.expectOne(`${environment.apiUrl}/categorias`).flush([]);
    http.expectOne(esLibros).flush(paginaDe([libroPrueba(1, { descripcion: 'Una gran historia' })]));
    await fixture.whenStable();

    el.querySelector<HTMLButtonElement>('.card__imagen')!.click();
    await fixture.whenStable();
    expect(el.querySelector('[role="dialog"]')?.textContent).toContain('Una gran historia');

    el.querySelector<HTMLButtonElement>('.dialogo__cerrar')!.click();
    await fixture.whenStable();
    expect(el.querySelector('[role="dialog"]')).toBeNull();
  });

  it('cambiar el orden vuelve a consultar con el nuevo criterio', async () => {
    const { fixture, http } = crear();
    http.expectOne(`${environment.apiUrl}/categorias`).flush([]);
    http.expectOne(esLibros).flush(paginaDe([libroPrueba(1)]));
    await fixture.whenStable();

    const select = (fixture.nativeElement as HTMLElement).querySelectorAll('select')[1] as HTMLSelectElement;
    select.value = 'precio,asc';
    select.dispatchEvent(new Event('change'));

    const req = http.expectOne(esLibros);
    expect(req.request.params.get('sort')).toBe('precio,asc');
    req.flush(paginaDe([]));
  });

  it('se actualiza sola cuando alguien publica, edita, elimina o compra un libro', async () => {
    const { fixture, el, http } = crear();
    http.expectOne(`${environment.apiUrl}/categorias`).flush([]);
    http.expectOne(esLibros).flush(paginaDe([libroPrueba(1)]));
    await fixture.whenStable();
    expect(el.querySelectorAll('app-libro-card').length).toBe(1);

    TestBed.inject(LibroService).notificarCambio(2);
    http.expectOne(esLibros).flush(paginaDe([libroPrueba(2), libroPrueba(1)]));
    await fixture.whenStable();

    expect(el.querySelectorAll('app-libro-card').length).toBe(2);
    expect(el.textContent).toContain('2 libros encontrados');
  });

  it('el refresco automático no muestra errores si falla', async () => {
    const { fixture, el, http } = crear();
    http.expectOne(`${environment.apiUrl}/categorias`).flush([]);
    http.expectOne(esLibros).flush(paginaDe([libroPrueba(1)]));
    await fixture.whenStable();

    TestBed.inject(LibroService).notificarCambio();
    http.expectOne(esLibros).flush(null, { status: 500, statusText: 'Server Error' });
    await fixture.whenStable();

    expect(el.querySelector('[role="alert"]')).toBeNull();
    expect(el.querySelectorAll('app-libro-card').length).toBe(1);
  });

  it('si la página actual desaparece (se eliminaron libros) retrocede a la última', async () => {
    const { fixture, el, http } = crear();
    http.expectOne(`${environment.apiUrl}/categorias`).flush([]);
    http.expectOne(esLibros).flush(paginaDe([libroPrueba(1)], 0, 2));
    await fixture.whenStable();
    fixture.detectChanges();

    el.querySelectorAll<HTMLButtonElement>('.paginador button')[1].click();
    http.expectOne(esLibros).flush(paginaDe([libroPrueba(2)], 1, 2));
    await fixture.whenStable();

    TestBed.inject(LibroService).notificarCambio();
    http.expectOne(esLibros).flush({ content: [], page: { size: 12, number: 1, totalElements: 1, totalPages: 1 } });
    const reintento = http.expectOne(esLibros);
    expect(reintento.request.params.get('page')).toBe('0');
    reintento.flush(paginaDe([libroPrueba(1)]));
  });
});
