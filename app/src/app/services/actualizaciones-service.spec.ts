import { TestBed } from '@angular/core/testing';
import { ActualizacionesService } from './actualizaciones-service';

describe('ActualizacionesService', () => {
  let servicio: ActualizacionesService;

  beforeEach(() => {
    servicio = TestBed.inject(ActualizacionesService);
  });

  it('avisa solo a quien escucha el recurso que cambió', () => {
    const catalogo = vi.fn();
    const pedidos = vi.fn();
    servicio.de('catalogo').subscribe(catalogo);
    servicio.de('pedidos').subscribe(pedidos);

    servicio.notificar('pedidos');

    expect(pedidos).toHaveBeenCalledTimes(1);
    expect(catalogo).not.toHaveBeenCalled();
  });

  it('un cambio propio se anuncia como local (para retransmitirlo a otras ventanas)', () => {
    const locales = vi.fn();
    servicio.locales$.subscribe(locales);

    servicio.notificar('catalogo');

    expect(locales).toHaveBeenCalledWith('catalogo');
  });

  it('un cambio externo refresca a todos pero no es local (evita bucles)', () => {
    const locales = vi.fn();
    const todos = vi.fn();
    servicio.locales$.subscribe(locales);
    servicio.de('usuarios').subscribe(todos);

    servicio.notificarExterno('usuarios');

    expect(todos).toHaveBeenCalledTimes(1);
    expect(locales).not.toHaveBeenCalled();
  });

  it('deOtros solo escucha los cambios que vienen de fuera', () => {
    const deOtros = vi.fn();
    servicio.deOtros('carrito').subscribe(deOtros);

    servicio.notificar('carrito');
    expect(deOtros).not.toHaveBeenCalled();

    servicio.notificarExterno('carrito');
    expect(deOtros).toHaveBeenCalledTimes(1);
  });

  it('esRecurso reconoce los nombres válidos y descarta el resto', () => {
    expect(servicio.esRecurso('pedidos')).toBe(true);
    expect(servicio.esRecurso('basura')).toBe(false);
    expect(servicio.esRecurso(42)).toBe(false);
  });
});
