import { TestBed } from '@angular/core/testing';
import { environment } from '../../environments/environment';
import { ActualizacionesService, Recurso } from './actualizaciones-service';
import {
  CanalMensajes,
  FABRICA_CANAL,
  FABRICA_EVENT_SOURCE,
  FuenteEventos,
  SincronizacionService,
} from './sincronizacion-service';

class FuenteFalsa implements FuenteEventos {
  onopen: ((e: Event) => void) | null = null;
  escuchas = new Map<string, () => void>();
  cerrada = false;
  constructor(readonly url: string) {}
  addEventListener(tipo: string, escucha: () => void): void {
    this.escuchas.set(tipo, escucha);
  }
  close(): void {
    this.cerrada = true;
  }
  emitir(tipo: string): void {
    this.escuchas.get(tipo)?.();
  }
}

class CanalFalso implements CanalMensajes {
  onmessage: ((e: MessageEvent) => void) | null = null;
  enviados: unknown[] = [];
  postMessage(mensaje: unknown): void {
    this.enviados.push(mensaje);
  }
  close(): void {}
}

function crear(opciones: { fuente?: FuenteFalsa | null; canal?: CanalFalso | null } = {}) {
  const fuente = opciones.fuente === undefined ? new FuenteFalsa('') : opciones.fuente;
  const canal = opciones.canal === undefined ? new CanalFalso() : opciones.canal;
  const urls: string[] = [];
  TestBed.configureTestingModule({
    providers: [
      { provide: FABRICA_EVENT_SOURCE, useValue: (url: string) => (urls.push(url), fuente) },
      { provide: FABRICA_CANAL, useValue: () => canal },
    ],
  });
  const actualizaciones = TestBed.inject(ActualizacionesService);
  const recibidos: Recurso[] = [];
  (['catalogo', 'pedidos', 'usuarios', 'carrito'] as const).forEach((r) =>
    actualizaciones.de(r).subscribe(() => recibidos.push(r)),
  );
  const servicio = TestBed.inject(SincronizacionService);
  return { servicio, actualizaciones, recibidos, fuente, canal, urls };
}

describe('SincronizacionService', () => {
  it('se suscribe a los eventos del servidor una sola vez', () => {
    const { servicio, urls } = crear();

    servicio.iniciar();
    servicio.iniciar();

    expect(urls).toEqual([`${environment.apiUrl}/eventos`]);
  });

  it('cada aviso del servidor refresca solo el recurso que cambió', () => {
    const { servicio, recibidos, fuente } = crear();
    servicio.iniciar();

    fuente!.emitir('catalogo');
    fuente!.emitir('pedidos');
    fuente!.emitir('usuarios');

    expect(recibidos).toEqual(['catalogo', 'pedidos', 'usuarios']);
  });

  it('al reconectarse refresca todo (pudo perderse algún aviso), pero no en la primera conexión', () => {
    const { servicio, recibidos, fuente } = crear();
    servicio.iniciar();

    fuente!.onopen?.(new Event('open'));
    expect(recibidos).toEqual([]);

    fuente!.onopen?.(new Event('open'));
    expect(recibidos).toEqual(['catalogo', 'pedidos', 'usuarios']);
  });

  it('un cambio hecho en esta ventana se avisa a las demás ventanas, indicando qué cambió', () => {
    const { servicio, actualizaciones, canal } = crear();
    servicio.iniciar();

    actualizaciones.notificar('carrito');
    actualizaciones.notificar('pedidos');

    expect(canal!.enviados).toEqual(['carrito', 'pedidos']);
  });

  it('un aviso que llega de otra ventana refresca ese recurso sin reenviarse (sin bucles)', () => {
    const { servicio, recibidos, canal } = crear();
    servicio.iniciar();

    canal!.onmessage?.(new MessageEvent('message', { data: 'carrito' }));

    expect(recibidos).toEqual(['carrito']);
    expect(canal!.enviados).toEqual([]);
  });

  it('ignora mensajes desconocidos del canal', () => {
    const { servicio, recibidos, canal } = crear();
    servicio.iniciar();

    canal!.onmessage?.(new MessageEvent('message', { data: 'cualquier-cosa' }));
    canal!.onmessage?.(new MessageEvent('message', { data: { x: 1 } }));

    expect(recibidos).toEqual([]);
  });

  it('un aviso del servidor no se reenvía a las demás ventanas', () => {
    const { servicio, fuente, canal } = crear();
    servicio.iniciar();

    fuente!.emitir('catalogo');

    expect(canal!.enviados).toEqual([]);
  });

  it('funciona sin EventSource ni BroadcastChannel (navegadores antiguos)', () => {
    const { servicio, actualizaciones, recibidos } = crear({ fuente: null, canal: null });

    expect(() => servicio.iniciar()).not.toThrow();
    actualizaciones.notificar('catalogo');

    expect(recibidos).toEqual(['catalogo']);
  });
});
