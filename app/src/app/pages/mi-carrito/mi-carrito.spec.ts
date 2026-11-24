import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { environment } from '../../../environments/environment';
import { Carrito } from '../../core/models';
import { ActualizacionesService } from '../../services/actualizaciones-service';
import { ToastService } from '../../services/toast-service';
import { iniciarSesion } from '../../testing/ayudas';
import { MiCarrito } from './mi-carrito';

const carrito: Carrito = {
  items: [{ libroId: 7, titulo: 'Dune', imagen: null, precioUnitario: 20, cantidad: 2, subtotal: 40, stockDisponible: 2, disponible: true }],
  total: 40,
};

async function crear(contenido: Carrito = carrito) {
  localStorage.clear();
  iniciarSesion();
  TestBed.configureTestingModule({
    imports: [MiCarrito],
    providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
  });
  const fixture = TestBed.createComponent(MiCarrito);
  fixture.detectChanges();
  const http = TestBed.inject(HttpTestingController);
  http.match(`${environment.apiUrl}/carrito`).forEach((r) => r.flush(contenido));
  await fixture.whenStable();
  fixture.detectChanges();
  return { fixture, el: fixture.nativeElement as HTMLElement, http, router: TestBed.inject(Router) };
}

describe('MiCarrito', () => {
  it('muestra los libros, la cantidad y el total', async () => {
    const { el } = await crear();

    expect(el.textContent).toContain('Dune');
    expect(el.textContent).toContain('2 artículos');
    expect(el.querySelector('.fila--total')?.textContent).toContain('S/ 40.00');
  });

  it('muestra un mensaje cuando el carrito está vacío', async () => {
    const { el } = await crear({ items: [], total: 0 });

    expect(el.textContent).toContain('Tu carrito está vacío');
  });

  it('no deja superar el stock disponible', async () => {
    const { el, http } = await crear();

    el.querySelector<HTMLButtonElement>('[aria-label="Aumentar cantidad"]')!.click();

    http.expectNone(`${environment.apiUrl}/carrito/items/7`);
  });

  it('quitar un libro llama a la API', async () => {
    const { el, http } = await crear();

    el.querySelector<HTMLButtonElement>('.quitar')!.click();

    const req = http.expectOne(`${environment.apiUrl}/carrito/items/7`);
    expect(req.request.method).toBe('DELETE');
    req.flush({ items: [], total: 0 });
  });

  it('confirmar la compra registra el pedido y lleva a "Mis pedidos"', async () => {
    const { el, http, router } = await crear();
    const navegar = vi.spyOn(router, 'navigate').mockResolvedValue(true);

    el.querySelector<HTMLButtonElement>('.resumen .btn--primario')!.click();

    const req = http.expectOne(`${environment.apiUrl}/pedidos`);
    expect(req.request.method).toBe('POST');
    req.flush({ id: 12, fechaPedido: '2026-11-14T10:00:00', total: 40, estado: 'REGISTRADO', cliente: 'Ana Paz', correoCliente: 'ana@mail.com', detalles: [] });

    expect(navegar).toHaveBeenCalledWith(['/mis-pedidos']);
  });

  it('si el pedido falla muestra el motivo y vuelve a leer el carrito', async () => {
    const { el, http } = await crear();

    el.querySelector<HTMLButtonElement>('.resumen .btn--primario')!.click();
    http
      .expectOne(`${environment.apiUrl}/pedidos`)
      .flush({ detail: "Stock insuficiente o libro no disponible: 'Dune'" }, { status: 409, statusText: 'Conflict' });

    expect(TestBed.inject(ToastService).avisos()[0].texto).toContain('Stock insuficiente');
    http.expectOne(`${environment.apiUrl}/carrito`).flush(carrito);
  });

  it('se actualiza sola si otro usuario compra y baja el stock bajo lo que tienes en el carrito', async () => {
    const { fixture, el, http } = await crear();
    const boton = () => el.querySelector<HTMLButtonElement>('.resumen .btn--primario')!;
    expect(boton().disabled).toBe(false);

    TestBed.inject(ActualizacionesService).notificarExterno('catalogo');
    http.expectOne(`${environment.apiUrl}/carrito`).flush({
      items: [{ ...carrito.items[0], stockDisponible: 1 }],
      total: 40,
    });
    await fixture.whenStable();
    fixture.detectChanges();

    expect(el.querySelector('.aviso-item')?.textContent).toContain('Solo quedan 1');
    expect(boton().disabled).toBe(true);
    expect(el.querySelector('.nota--error')).toBeTruthy();
  });

  it('avisa y bloquea la compra si el vendedor dio de baja un libro del carrito', async () => {
    const { fixture, el, http } = await crear();

    TestBed.inject(ActualizacionesService).notificarExterno('catalogo');
    http.expectOne(`${environment.apiUrl}/carrito`).flush({
      items: [{ ...carrito.items[0], disponible: false }],
      total: 40,
    });
    await fixture.whenStable();
    fixture.detectChanges();

    expect(el.querySelector('.aviso-item')?.textContent).toContain('ya no está disponible');
    expect(el.querySelector<HTMLButtonElement>('.resumen .btn--primario')!.disabled).toBe(true);
  });

  it('si el mismo usuario cambia el carrito en otra ventana, esta se actualiza', async () => {
    const { fixture, el, http } = await crear();

    TestBed.inject(ActualizacionesService).notificarExterno('carrito');
    http.expectOne(`${environment.apiUrl}/carrito`).flush({ items: [], total: 0 });
    await fixture.whenStable();
    fixture.detectChanges();

    expect(el.textContent).toContain('Tu carrito está vacío');
  });
});
