import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../environments/environment';
import { Carrito } from '../core/models';
import { ActualizacionesService } from './actualizaciones-service';
import { CarritoService } from './carrito-service';

const carrito: Carrito = {
  items: [
    { libroId: 1, titulo: 'Dune', imagen: null, precioUnitario: 20, cantidad: 2, subtotal: 40, stockDisponible: 5, disponible: true },
    { libroId: 2, titulo: '1984', imagen: null, precioUnitario: 10, cantidad: 1, subtotal: 10, stockDisponible: 3, disponible: true },
  ],
  total: 50,
};

function crear(conSesion = false): { servicio: CarritoService; http: HttpTestingController } {
  localStorage.clear();
  if (conSesion) {
    localStorage.setItem('nl.token', 'T');
    localStorage.setItem('nl.usuario', JSON.stringify({ id: 1, nombres: 'Ana', apellidos: 'Paz', correo: 'a@b.com', rol: 'CLIENTE' }));
  }
  TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
  return { servicio: TestBed.inject(CarritoService), http: TestBed.inject(HttpTestingController) };
}

describe('CarritoService', () => {
  it('comienza vacío', () => {
    const { servicio } = crear();
    TestBed.tick();
    expect(servicio.cantidadItems()).toBe(0);
    expect(servicio.carrito().total).toBe(0);
  });

  it('agregar envía libroId y cantidad y actualiza el estado', () => {
    const { servicio, http } = crear();

    servicio.agregar(1, 2).subscribe();
    const req = http.expectOne(`${environment.apiUrl}/carrito/items`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ libroId: 1, cantidad: 2 });
    req.flush(carrito);

    expect(servicio.cantidadItems()).toBe(3);
    expect(servicio.carrito().total).toBe(50);
  });

  it('actualizarCantidad usa PUT sobre el libro', () => {
    const { servicio, http } = crear();

    servicio.actualizarCantidad(2, 4).subscribe();
    const req = http.expectOne(`${environment.apiUrl}/carrito/items/2`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual({ cantidad: 4 });
    req.flush(carrito);
  });

  it('eliminar usa DELETE y refleja la respuesta', () => {
    const { servicio, http } = crear();

    servicio.eliminar(1).subscribe();
    http.expectOne(`${environment.apiUrl}/carrito/items/1`).flush({ items: [carrito.items[1]], total: 10 });

    expect(servicio.cantidadItems()).toBe(1);
  });

  it('reiniciar deja el carrito vacío', () => {
    const { servicio, http } = crear();
    servicio.agregar(1, 2).subscribe();
    http.expectOne(`${environment.apiUrl}/carrito/items`).flush(carrito);

    servicio.reiniciar();

    expect(servicio.cantidadItems()).toBe(0);
  });

  it('con sesión iniciada carga el carrito automáticamente', () => {
    const { servicio, http } = crear(true);

    TestBed.tick();
    http.expectOne(`${environment.apiUrl}/carrito`).flush(carrito);

    expect(servicio.cantidadItems()).toBe(3);
  });

  describe('reactividad', () => {
    it('los cambios propios se avisan a las demás ventanas (carrito)', () => {
      const { servicio, http } = crear();
      const locales = vi.fn();
      TestBed.inject(ActualizacionesService).locales$.subscribe(locales);

      servicio.agregar(1, 1).subscribe();
      http.expectOne(`${environment.apiUrl}/carrito/items`).flush(carrito);

      expect(locales).toHaveBeenCalledWith('carrito');
    });

    it('un cambio propio no provoca una lectura extra del carrito', () => {
      const { servicio, http } = crear(true);
      TestBed.tick();
      http.expectOne(`${environment.apiUrl}/carrito`).flush(carrito);

      servicio.agregar(1, 1).subscribe();
      http.expectOne(`${environment.apiUrl}/carrito/items`).flush(carrito);

      http.expectNone(`${environment.apiUrl}/carrito`);
    });

    it('si el mismo usuario cambia el carrito en otra ventana, se vuelve a leer', () => {
      const { servicio, http } = crear(true);
      TestBed.tick();
      http.expectOne(`${environment.apiUrl}/carrito`).flush({ items: [], total: 0 });

      TestBed.inject(ActualizacionesService).notificarExterno('carrito');
      http.expectOne(`${environment.apiUrl}/carrito`).flush(carrito);

      expect(servicio.cantidadItems()).toBe(3);
    });

    it('si cambia el catálogo (stock, precio, baja de un libro) se vuelve a leer para mostrar lo vigente', () => {
      const { servicio, http } = crear(true);
      TestBed.tick();
      http.expectOne(`${environment.apiUrl}/carrito`).flush(carrito);

      TestBed.inject(ActualizacionesService).notificarExterno('catalogo');
      http.expectOne(`${environment.apiUrl}/carrito`).flush({
        items: [{ ...carrito.items[0], stockDisponible: 1, disponible: false }],
        total: 40,
      });

      expect(servicio.carrito().items[0].disponible).toBe(false);
    });

    it('sin sesión no consulta el carrito cuando algo cambia', () => {
      const { http } = crear(false);
      TestBed.tick();

      TestBed.inject(ActualizacionesService).notificarExterno('catalogo');

      http.expectNone(`${environment.apiUrl}/carrito`);
    });
  });
});
