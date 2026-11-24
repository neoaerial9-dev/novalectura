import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../environments/environment';
import { ActualizacionesService } from '../../services/actualizaciones-service';
import { paginaDe, pedidoPrueba } from '../../testing/ayudas';
import { AdminPedidos } from './admin-pedidos';

const esPedidos = (r: { url: string }) => r.url === `${environment.apiUrl}/pedidos`;

function crear() {
  localStorage.clear();
  TestBed.configureTestingModule({
    imports: [AdminPedidos],
    providers: [provideHttpClient(), provideHttpClientTesting()],
  });
  const fixture = TestBed.createComponent(AdminPedidos);
  fixture.detectChanges();
  return { fixture, el: fixture.nativeElement as HTMLElement, http: TestBed.inject(HttpTestingController) };
}

describe('AdminPedidos', () => {
  it('muestra los pedidos con los datos del cliente', async () => {
    const { fixture, el, http } = crear();

    http.expectOne(esPedidos).flush(paginaDe([pedidoPrueba(1)]));
    await fixture.whenStable();

    expect(el.textContent).toContain('Pedido #1');
    expect(el.textContent).toContain('ana@mail.com');
    expect(el.textContent).toContain('1 pedido registrado');
  });

  it('cuando alguien hace un pedido, aparece solo y en la misma página', async () => {
    const { fixture, el, http } = crear();
    http.expectOne(esPedidos).flush(paginaDe([pedidoPrueba(1)], 0, 3));
    await fixture.whenStable();
    fixture.detectChanges();
    el.querySelectorAll<HTMLButtonElement>('.paginador button')[1].click();
    http.expectOne(esPedidos).flush(paginaDe([pedidoPrueba(2)], 1, 3));
    await fixture.whenStable();

    TestBed.inject(ActualizacionesService).notificarExterno('pedidos');
    const req = http.expectOne(esPedidos);
    expect(req.request.params.get('page')).toBe('1');
    req.flush(paginaDe([pedidoPrueba(3), pedidoPrueba(2)], 1, 3));
    await fixture.whenStable();

    expect(el.querySelectorAll('app-pedido-tarjeta').length).toBe(2);
  });

  it('un refresco automático fallido no reemplaza la lista por un error', async () => {
    const { fixture, el, http } = crear();
    http.expectOne(esPedidos).flush(paginaDe([pedidoPrueba(1)]));
    await fixture.whenStable();

    TestBed.inject(ActualizacionesService).notificarExterno('pedidos');
    http.expectOne(esPedidos).flush(null, { status: 500, statusText: 'Server Error' });
    await fixture.whenStable();

    expect(el.querySelector('[role="alert"]')).toBeNull();
    expect(el.querySelectorAll('app-pedido-tarjeta').length).toBe(1);
  });
});
