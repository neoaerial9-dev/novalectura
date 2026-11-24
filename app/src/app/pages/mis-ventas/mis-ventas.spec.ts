import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { environment } from '../../../environments/environment';
import { ActualizacionesService } from '../../services/actualizaciones-service';
import { ventaPrueba } from '../../testing/ayudas';
import { MisVentas } from './mis-ventas';

const url = `${environment.apiUrl}/mis-ventas`;

function crear() {
  localStorage.clear();
  TestBed.configureTestingModule({
    imports: [MisVentas],
    providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
  });
  const fixture = TestBed.createComponent(MisVentas);
  fixture.detectChanges();
  return { fixture, el: fixture.nativeElement as HTMLElement, http: TestBed.inject(HttpTestingController) };
}

describe('MisVentas', () => {
  it('lista las ventas con el total vendido', async () => {
    const { fixture, el, http } = crear();

    http.expectOne(url).flush([ventaPrueba(1), ventaPrueba(2, { subtotal: 30 })]);
    await fixture.whenStable();

    expect(el.querySelectorAll('tbody tr').length).toBe(2);
    expect(el.textContent).toContain('S/ 50.00');
    expect(el.querySelector('td[data-etiqueta="Comprador"]')?.textContent).toContain('Luis Gil');
  });

  it('una venta nueva (alguien compró uno de tus libros) aparece sola', async () => {
    const { fixture, el, http } = crear();
    http.expectOne(url).flush([]);
    await fixture.whenStable();
    expect(el.textContent).toContain('Todavía no tienes ventas');

    TestBed.inject(ActualizacionesService).notificarExterno('pedidos');
    http.expectOne(url).flush([ventaPrueba(9)]);
    await fixture.whenStable();

    expect(el.querySelectorAll('tbody tr').length).toBe(1);
  });
});
