import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { environment } from '../../../environments/environment';
import { ActualizacionesService } from '../../services/actualizaciones-service';
import { paginaDe, usuarioPrueba } from '../../testing/ayudas';
import { AdminUsuarios } from './admin-usuarios';

const esUsuarios = (r: { url: string }) => r.url === `${environment.apiUrl}/usuarios`;

function crear() {
  localStorage.clear();
  TestBed.configureTestingModule({
    imports: [AdminUsuarios],
    providers: [provideHttpClient(), provideHttpClientTesting()],
  });
  const fixture = TestBed.createComponent(AdminUsuarios);
  fixture.detectChanges();
  return { fixture, el: fixture.nativeElement as HTMLElement, http: TestBed.inject(HttpTestingController) };
}

describe('AdminUsuarios', () => {
  it('lista los usuarios registrados', async () => {
    const { fixture, el, http } = crear();

    http.expectOne(esUsuarios).flush(paginaDe([usuarioPrueba]));
    await fixture.whenStable();

    expect(el.querySelectorAll('tbody tr').length).toBe(1);
    expect(el.querySelector('td[data-etiqueta="Correo"]')?.textContent).toContain('ana@mail.com');
  });

  it('cuando alguien se registra, aparece solo en la lista', async () => {
    const { fixture, el, http } = crear();
    http.expectOne(esUsuarios).flush(paginaDe([usuarioPrueba]));
    await fixture.whenStable();

    TestBed.inject(ActualizacionesService).notificarExterno('usuarios');
    http.expectOne(esUsuarios).flush(paginaDe([usuarioPrueba, { ...usuarioPrueba, id: 2, correo: 'luis@mail.com' }]));
    await fixture.whenStable();

    expect(el.querySelectorAll('tbody tr').length).toBe(2);
    expect(el.textContent).toContain('2 usuarios registrados');
  });
});
