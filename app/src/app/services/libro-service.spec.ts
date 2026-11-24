import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../environments/environment';
import { ActualizacionesService } from './actualizaciones-service';
import { LibroService } from './libro-service';

describe('LibroService', () => {
  let servicio: LibroService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    servicio = TestBed.inject(LibroService);
    http = TestBed.inject(HttpTestingController);
  });

  it('listar envía solo los filtros con valor', () => {
    servicio.listar({ q: '  dune ', categoriaId: 3, page: 1, size: 12 }).subscribe();

    const req = http.expectOne((r) => r.url === `${environment.apiUrl}/libros`);
    expect(req.request.params.get('q')).toBe('dune');
    expect(req.request.params.get('categoriaId')).toBe('3');
    expect(req.request.params.get('page')).toBe('1');
    expect(req.request.params.get('size')).toBe('12');
    req.flush({ content: [], page: { size: 12, number: 1, totalElements: 0, totalPages: 0 } });
  });

  it('listar omite búsqueda vacía y categoría nula', () => {
    servicio.listar({ q: '   ', categoriaId: null }).subscribe();

    const req = http.expectOne((r) => r.url === `${environment.apiUrl}/libros`);
    expect(req.request.params.keys()).toEqual([]);
    req.flush({ content: [], page: { size: 12, number: 0, totalElements: 0, totalPages: 0 } });
  });

  it('eliminar usa DELETE /libros/{id}', () => {
    servicio.eliminar(7).subscribe();
    const req = http.expectOne(`${environment.apiUrl}/libros/7`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });

  it('misLibros consulta /mis-libros', () => {
    servicio.misLibros().subscribe();
    http.expectOne(`${environment.apiUrl}/mis-libros`).flush([]);
  });

  it('listar envía el orden elegido', () => {
    servicio.listar({ sort: 'precio,asc' }).subscribe();
    const req = http.expectOne((r) => r.url === `${environment.apiUrl}/libros`);
    expect(req.request.params.get('sort')).toBe('precio,asc');
    req.flush({ content: [], page: { size: 12, number: 0, totalElements: 0, totalPages: 0 } });
  });

  describe('reactividad', () => {
    const solicitud = {
      titulo: 'Dune', autor: 'F. Herbert', editorial: null, isbn: null, anioPublicacion: null, descripcion: null,
      precio: 10, stock: 1, imagen: null, condicion: 'BUENO' as const, categoriaId: 1,
    };

    it('crear avisa del cambio y resalta el libro nuevo', () => {
      const avisos = vi.fn();
      servicio.cambios$.subscribe(avisos);

      servicio.crear(solicitud).subscribe();
      http.expectOne(`${environment.apiUrl}/libros`).flush({ id: 42 });

      expect(avisos).toHaveBeenCalledTimes(1);
      expect(servicio.resaltado()).toBe(42);
    });

    it('actualizar y eliminar también avisan del cambio', () => {
      const avisos = vi.fn();
      servicio.cambios$.subscribe(avisos);

      servicio.actualizar(5, solicitud).subscribe();
      http.expectOne(`${environment.apiUrl}/libros/5`).flush({ id: 5 });
      servicio.eliminar(5).subscribe();
      http.expectOne(`${environment.apiUrl}/libros/5`).flush(null);

      expect(avisos).toHaveBeenCalledTimes(2);
    });

    it('los cambios propios se anuncian como locales (para retransmitirlos a otras ventanas)', () => {
      const locales = vi.fn();
      TestBed.inject(ActualizacionesService).locales$.subscribe(locales);

      servicio.notificarCambio();

      expect(locales).toHaveBeenCalledWith('catalogo');
    });

    it('los cambios externos del catálogo también refrescan las listas, sin resaltar nada', () => {
      const avisos = vi.fn();
      servicio.cambios$.subscribe(avisos);

      TestBed.inject(ActualizacionesService).notificarExterno('catalogo');

      expect(avisos).toHaveBeenCalledTimes(1);
      expect(servicio.resaltado()).toBeNull();
    });

    it('el resaltado se quita solo pasados unos segundos', () => {
      vi.useFakeTimers();
      servicio.notificarCambio(7);
      expect(servicio.resaltado()).toBe(7);

      vi.advanceTimersByTime(4100);

      expect(servicio.resaltado()).toBeNull();
      vi.useRealTimers();
    });
  });
});
