import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../environments/environment';
import { ActualizacionesService, Recurso } from './actualizaciones-service';
import { PedidoService } from './pedido-service';

describe('PedidoService', () => {
  it('al registrar un pedido avisa que cambiaron el catálogo (stock), los pedidos y el carrito', () => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    const http = TestBed.inject(HttpTestingController);
    const actualizaciones = TestBed.inject(ActualizacionesService);
    const avisos: Recurso[] = [];
    (['catalogo', 'pedidos', 'carrito', 'usuarios'] as const).forEach((r) => actualizaciones.de(r).subscribe(() => avisos.push(r)));

    TestBed.inject(PedidoService).registrar().subscribe();
    http.expectOne(`${environment.apiUrl}/pedidos`).flush({ id: 1 });

    expect(avisos).toEqual(['catalogo', 'pedidos', 'carrito']);
  });

  it('si el pedido falla no avisa de nada', () => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    const http = TestBed.inject(HttpTestingController);
    const aviso = vi.fn();
    TestBed.inject(ActualizacionesService).de('pedidos').subscribe(aviso);

    TestBed.inject(PedidoService).registrar().subscribe({ error: () => undefined });
    http.expectOne(`${environment.apiUrl}/pedidos`).flush({}, { status: 409, statusText: 'Conflict' });

    expect(aviso).not.toHaveBeenCalled();
  });
});
