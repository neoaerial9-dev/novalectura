import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { iniciarSesion, libroPrueba } from '../../testing/ayudas';
import { LibroCard } from './libro-card';

function crear(libro = libroPrueba(1), conSesion = false) {
  localStorage.clear();
  if (conSesion) {
    iniciarSesion();
  }
  TestBed.configureTestingModule({ imports: [LibroCard], providers: [provideHttpClient(), provideHttpClientTesting()] });
  const fixture = TestBed.createComponent(LibroCard);
  fixture.componentRef.setInput('libro', libro);
  fixture.detectChanges();
  return { fixture, el: fixture.nativeElement as HTMLElement };
}

describe('LibroCard', () => {
  it('muestra título, autor, categoría, condición, vendedor y precio', () => {
    const { el } = crear(libroPrueba(1, { titulo: 'Dune', autor: 'Frank Herbert', precio: 24.99 }));

    expect(el.textContent).toContain('Dune');
    expect(el.textContent).toContain('Frank Herbert');
    expect(el.textContent).toContain('Ficción');
    expect(el.textContent).toContain('Bueno · Vendedor Uno');
    expect(el.textContent).toContain('S/ 24.99');
  });

  it('emite agregar al pulsar "+ Carrito"', () => {
    const { fixture, el } = crear();
    const emitido = vi.fn();
    fixture.componentInstance.agregar.subscribe(emitido);

    el.querySelector<HTMLButtonElement>('.card__pie button')!.click();

    expect(emitido).toHaveBeenCalledWith(expect.objectContaining({ id: 1 }));
  });

  it('desactiva el botón si el libro es del propio usuario', () => {
    const { el } = crear(libroPrueba(1, { vendedorId: 1 }), true);
    const boton = el.querySelector<HTMLButtonElement>('.card__pie button')!;

    expect(boton.disabled).toBe(true);
    expect(boton.textContent).toContain('Tuyo');
  });

  it('desactiva el botón si no hay stock', () => {
    const { el } = crear(libroPrueba(1, { stock: 0 }));
    const boton = el.querySelector<HTMLButtonElement>('.card__pie button')!;

    expect(boton.disabled).toBe(true);
    expect(boton.textContent).toContain('Agotado');
  });

  it('usa una portada de reemplazo si el libro no tiene imagen', () => {
    const { el } = crear(libroPrueba(1, { imagen: null }));

    expect(el.querySelector('img')!.getAttribute('src')).toContain('data:image/svg+xml');
  });
});
