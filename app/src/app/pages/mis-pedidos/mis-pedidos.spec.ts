import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { environment } from '../../../environments/environment';
import { ActualizacionesService } from '../../services/actualizaciones-service';
import { pedidoPrueba } from '../../testing/ayudas';
import { MisPedidos } from './mis-pedidos';

const url = `${environment.apiUrl}/pedidos/mis-pedidos`;

function crear() {
  localStorage.clear();
  TestBed.configureTestingModule({
    imports: [MisPedidos],
    providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
  });
  const fixture = TestBed.createComponent(MisPedidos);
  fixture.detectChanges();
  return { fixture, el: fixture.nativeElement as HTMLElement, http: TestBed.inject(HttpTestingController) };
}

describe('MisPedidos', () => {
  it('lista los pedidos del usuario', async () => {
    const { fixture, el, http } = crear();

    http.expectOne(url).flush([pedidoPrueba(1), pedidoPrueba(2)]);
    await fixture.whenStable();

    expect(el.querySelectorAll('app-pedido-tarjeta').length).toBe(2);
  });

  it('muestra el estado vacío', async () => {
    const { fixture, el, http } = crear();

    http.expectOne(url).flush([]);
    await fixture.whenStable();

    expect(el.textContent).toContain('Aún no tienes pedidos');
  });

  it('un pedido nuevo aparece solo, sin recargar', async () => {
    const { fixture, el, http } = crear();
    http.expectOne(url).flush([pedidoPrueba(1)]);
    await fixture.whenStable();

    TestBed.inject(ActualizacionesService).notificarExterno('pedidos');
    http.expectOne(url).flush([pedidoPrueba(2), pedidoPrueba(1)]);
    await fixture.whenStable();

    expect(el.querySelectorAll('app-pedido-tarjeta').length).toBe(2);
  });

  it('si falla un refresco automático conserva lo que ya mostraba y no muestra error', async () => {
    const { fixture, el, http } = crear();
    http.expectOne(url).flush([pedidoPrueba(1)]);
    await fixture.whenStable();

    TestBed.inject(ActualizacionesService).notificarExterno('pedidos');
    http.expectOne(url).flush(null, { status: 500, statusText: 'Server Error' });
    await fixture.whenStable();

    expect(el.querySelector('[role="alert"]')).toBeNull();
    expect(el.querySelectorAll('app-pedido-tarjeta').length).toBe(1);
  });

  it('ignora cambios de otros recursos', async () => {
    const { fixture, http } = crear();
    http.expectOne(url).flush([]);
    await fixture.whenStable();

    TestBed.inject(ActualizacionesService).notificarExterno('usuarios');

    http.expectNone(url);
  });
});
