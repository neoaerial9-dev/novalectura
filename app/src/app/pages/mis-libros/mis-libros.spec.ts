import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { environment } from '../../../environments/environment';
import { LibroService } from '../../services/libro-service';
import { libroPrueba } from '../../testing/ayudas';
import { MisLibros } from './mis-libros';

function crear() {
  localStorage.clear();
  TestBed.configureTestingModule({
    imports: [MisLibros],
    providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
  });
  const fixture = TestBed.createComponent(MisLibros);
  fixture.detectChanges();
  return { fixture, el: fixture.nativeElement as HTMLElement, http: TestBed.inject(HttpTestingController) };
}

const url = `${environment.apiUrl}/mis-libros`;

describe('MisLibros', () => {
  it('lista los libros publicados por el usuario', async () => {
    const { fixture, el, http } = crear();

    http.expectOne(url).flush([libroPrueba(1), libroPrueba(2)]);
    await fixture.whenStable();

    expect(el.querySelectorAll('tbody tr').length).toBe(2);
    expect(el.querySelector('td[data-etiqueta="Título"]')?.textContent).toContain('Libro 1');
  });

  it('muestra el estado vacío con la invitación a publicar', async () => {
    const { fixture, el, http } = crear();

    http.expectOne(url).flush([]);
    await fixture.whenStable();

    expect(el.textContent).toContain('Aún no has publicado libros');
  });

  it('se actualiza sola al publicar un libro y resalta el nuevo', async () => {
    const { fixture, el, http } = crear();
    http.expectOne(url).flush([libroPrueba(1)]);
    await fixture.whenStable();

    TestBed.inject(LibroService).notificarCambio(2);
    http.expectOne(url).flush([libroPrueba(2), libroPrueba(1)]);
    await fixture.whenStable();
    fixture.detectChanges();

    const filas = el.querySelectorAll('tbody tr');
    expect(filas.length).toBe(2);
    expect(filas[0].classList.contains('resaltada')).toBe(true);
    expect(filas[1].classList.contains('resaltada')).toBe(false);
  });

  it('eliminar pide confirmación y llama a la API', async () => {
    const { fixture, el, http } = crear();
    http.expectOne(url).flush([libroPrueba(1)]);
    await fixture.whenStable();
    vi.spyOn(window, 'confirm').mockReturnValue(true);

    el.querySelector<HTMLButtonElement>('.btn-enlace.peligro')!.click();

    const req = http.expectOne(`${environment.apiUrl}/libros/1`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
    http.expectOne(url).flush([]);
  });
});
