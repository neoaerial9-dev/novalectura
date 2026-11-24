import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { App } from './app';
import { FABRICA_CANAL, FABRICA_EVENT_SOURCE } from './services/sincronizacion-service';

describe('App', () => {
  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      imports: [App],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: FABRICA_EVENT_SOURCE, useValue: () => null },
        { provide: FABRICA_CANAL, useValue: () => null },
      ],
    });
  });

  it('crea la aplicación con barra de navegación y pie de página', async () => {
    const fixture = TestBed.createComponent(App);
    await fixture.whenStable();
    const el = fixture.nativeElement as HTMLElement;

    expect(el.querySelector('app-navbar')).toBeTruthy();
    expect(el.querySelector('app-footer')).toBeTruthy();
    expect(el.textContent).toContain('Tienda de Libros');
  });

  it('ofrece un enlace para saltar al contenido que enfoca el área principal', async () => {
    const fixture = TestBed.createComponent(App);
    await fixture.whenStable();
    const el = fixture.nativeElement as HTMLElement;
    const enlace = el.querySelector<HTMLAnchorElement>('.saltar')!;
    const principal = el.querySelector<HTMLElement>('main#contenido')!;

    enlace.click();

    expect(principal.getAttribute('tabindex')).toBe('-1');
    expect(document.activeElement).toBe(principal);
  });
});
