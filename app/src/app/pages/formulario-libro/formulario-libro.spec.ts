import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { environment } from '../../../environments/environment';
import { enviarFormulario, escribir, libroPrueba } from '../../testing/ayudas';
import { FormularioLibro } from './formulario-libro';

function crear(id?: string) {
  localStorage.clear();
  TestBed.configureTestingModule({
    imports: [FormularioLibro],
    providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
  });
  const fixture = TestBed.createComponent(FormularioLibro);
  if (id) {
    fixture.componentRef.setInput('id', id);
  }
  fixture.detectChanges();
  const http = TestBed.inject(HttpTestingController);
  return { fixture, el: fixture.nativeElement as HTMLElement, http, router: TestBed.inject(Router) };
}

describe('FormularioLibro', () => {
  it('no envía nada si faltan datos obligatorios', async () => {
    const { fixture, el, http } = crear();
    http.expectOne(`${environment.apiUrl}/categorias`).flush([{ id: 1, nombre: 'Ficción', descripcion: null }]);

    enviarFormulario(fixture);
    await fixture.whenStable();

    http.expectNone(`${environment.apiUrl}/libros`);
    expect(el.textContent).toContain('El título es obligatorio');
    expect(el.textContent).toContain('Selecciona una categoría');
  });

  it('publica un libro nuevo con los datos del formulario', async () => {
    const { fixture, http, router } = crear();
    const navegar = vi.spyOn(router, 'navigate').mockResolvedValue(true);
    http.expectOne(`${environment.apiUrl}/categorias`).flush([{ id: 1, nombre: 'Ficción', descripcion: null }]);
    await fixture.whenStable();
    fixture.detectChanges();

    escribir(fixture, '#titulo', '  Dune ');
    escribir(fixture, '#autor', 'Frank Herbert');
    escribir(fixture, '#precio', '24.99');
    escribir(fixture, '#stock', '4');
    const categoria = (fixture.nativeElement as HTMLElement).querySelector<HTMLSelectElement>('#categoria')!;
    categoria.value = categoria.options[1].value;
    categoria.dispatchEvent(new Event('change'));

    enviarFormulario(fixture);
    const req = http.expectOne(`${environment.apiUrl}/libros`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({
      titulo: 'Dune',
      autor: 'Frank Herbert',
      editorial: null,
      isbn: null,
      anioPublicacion: null,
      descripcion: null,
      precio: 24.99,
      stock: 4,
      imagen: null,
      condicion: 'BUENO',
      categoriaId: 1,
    });
    req.flush(libroPrueba(1));

    expect(navegar).toHaveBeenCalledWith(['/vender']);
  });

  it('en modo edición carga el libro y lo actualiza con PUT', async () => {
    const { fixture, el, http, router } = crear('5');
    const navegar = vi.spyOn(router, 'navigate').mockResolvedValue(true);
    http.expectOne(`${environment.apiUrl}/categorias`).flush([{ id: 1, nombre: 'Ficción', descripcion: null }]);
    http.expectOne(`${environment.apiUrl}/libros/5`).flush(libroPrueba(5, { titulo: 'Dune', precio: 24.99 }));
    await fixture.whenStable();
    fixture.detectChanges();

    expect(el.querySelector<HTMLInputElement>('#titulo')!.value).toBe('Dune');
    expect(el.querySelector('h1')?.textContent).toContain('Editar libro');

    escribir(fixture, '#precio', '19.5');
    enviarFormulario(fixture);
    const req = http.expectOne(`${environment.apiUrl}/libros/5`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body.precio).toBe(19.5);
    req.flush(libroPrueba(5));

    expect(navegar).toHaveBeenCalledWith(['/vender']);
  });

  it('muestra el error de la API, por ejemplo un 403 al editar un libro ajeno', async () => {
    const { fixture, el, http } = crear('5');
    http.expectOne(`${environment.apiUrl}/categorias`).flush([]);
    http
      .expectOne(`${environment.apiUrl}/libros/5`)
      .flush({ detail: 'Solo el vendedor puede modificar este libro' }, { status: 403, statusText: 'Forbidden' });
    await fixture.whenStable();

    expect(el.textContent).toContain('Solo el vendedor puede modificar este libro');
  });

  function elegirArchivo(fixture: ReturnType<typeof crear>['fixture'], archivo: File): void {
    const campo = (fixture.nativeElement as HTMLElement).querySelector<HTMLInputElement>('#portada')!;
    Object.defineProperty(campo, 'files', { value: [archivo], configurable: true });
    campo.dispatchEvent(new Event('change'));
  }

  it('sube la portada elegida, muestra la vista previa y la incluye al guardar', async () => {
    const { fixture, el, http, router } = crear();
    vi.spyOn(router, 'navigate').mockResolvedValue(true);
    http.expectOne(`${environment.apiUrl}/categorias`).flush([{ id: 1, nombre: 'Ficción', descripcion: null }]);
    await fixture.whenStable();
    fixture.detectChanges();

    elegirArchivo(fixture, new File(['x'], 'portada.png', { type: 'image/png' }));
    const subida = http.expectOne(`${environment.apiUrl}/imagenes`);
    expect(subida.request.body).toBeInstanceOf(FormData);
    subida.flush({ ruta: '/api/imagenes/abc.png' });
    await fixture.whenStable();
    fixture.detectChanges();

    expect(el.querySelector('.vista-previa')!.getAttribute('src')).toContain('/api/imagenes/abc.png');

    escribir(fixture, '#titulo', 'Dune');
    escribir(fixture, '#autor', 'Frank Herbert');
    escribir(fixture, '#precio', '24.99');
    escribir(fixture, '#stock', '4');
    const categoria = el.querySelector<HTMLSelectElement>('#categoria')!;
    categoria.value = categoria.options[1].value;
    categoria.dispatchEvent(new Event('change'));
    enviarFormulario(fixture);

    const req = http.expectOne(`${environment.apiUrl}/libros`);
    expect(req.request.body.imagen).toBe('/api/imagenes/abc.png');
    req.flush(libroPrueba(1));
  });

  it('rechaza en el navegador un archivo que no es imagen, sin llamar a la API', async () => {
    const { fixture, el, http } = crear();
    http.expectOne(`${environment.apiUrl}/categorias`).flush([]);

    elegirArchivo(fixture, new File(['x'], 'datos.pdf', { type: 'application/pdf' }));
    await fixture.whenStable();
    fixture.detectChanges();

    http.expectNone(`${environment.apiUrl}/imagenes`);
    expect(el.textContent).toContain('JPG, PNG o WEBP');
  });

  it('muestra el error si la API rechaza la imagen', async () => {
    const { fixture, el, http } = crear();
    http.expectOne(`${environment.apiUrl}/categorias`).flush([]);

    elegirArchivo(fixture, new File(['x'], 'portada.png', { type: 'image/png' }));
    http
      .expectOne(`${environment.apiUrl}/imagenes`)
      .flush({ detail: 'Solo se permiten imágenes JPG, PNG o WEBP' }, { status: 400, statusText: 'Bad Request' });
    await fixture.whenStable();
    fixture.detectChanges();

    expect(el.querySelector('[role="alert"]')?.textContent).toContain('Solo se permiten imágenes');
  });

  it('permite quitar la portada ya subida', async () => {
    const { fixture, el, http } = crear('5');
    http.expectOne(`${environment.apiUrl}/categorias`).flush([]);
    http.expectOne(`${environment.apiUrl}/libros/5`).flush(libroPrueba(5, { imagen: '/api/imagenes/abc.png' }));
    await fixture.whenStable();
    fixture.detectChanges();

    el.querySelector<HTMLButtonElement>('.portada__controles .btn-enlace')!.click();
    await fixture.whenStable();
    fixture.detectChanges();

    expect(el.querySelector('.portada__controles .btn-enlace')).toBeNull();
  });
});
