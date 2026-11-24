import { InjectionToken, Service, inject } from '@angular/core';
import { environment } from '../../environments/environment';
import { ActualizacionesService, Recurso } from './actualizaciones-service';

export interface FuenteEventos {
  addEventListener(tipo: string, escucha: () => void): void;
  onopen: ((e: Event) => void) | null;
  close(): void;
}

export interface CanalMensajes {
  postMessage(mensaje: unknown): void;
  onmessage: ((e: MessageEvent) => void) | null;
  close(): void;
}

export const FABRICA_EVENT_SOURCE = new InjectionToken<(url: string) => FuenteEventos | null>('FABRICA_EVENT_SOURCE', {
  providedIn: 'root',
  factory: () => (url) => (typeof EventSource === 'undefined' ? null : new EventSource(url)),
});

export const FABRICA_CANAL = new InjectionToken<() => CanalMensajes | null>('FABRICA_CANAL', {
  providedIn: 'root',
  factory: () => () => (typeof BroadcastChannel === 'undefined' ? null : new BroadcastChannel('nl-catalogo')),
});

const EVENTOS_DEL_SERVIDOR: readonly Recurso[] = ['catalogo', 'pedidos', 'usuarios'];

@Service()
export class SincronizacionService {
  private readonly actualizaciones = inject(ActualizacionesService);
  private readonly crearFuente = inject(FABRICA_EVENT_SOURCE);
  private readonly crearCanal = inject(FABRICA_CANAL);
  private iniciado = false;

  iniciar(): void {
    if (this.iniciado) {
      return;
    }
    this.iniciado = true;
    this.escucharAlServidor();
    this.escucharAOtrasVentanas();
  }

  private escucharAlServidor(): void {
    const fuente = this.crearFuente(`${environment.apiUrl}/eventos`);
    if (!fuente) {
      return;
    }
    let yaSeAbrio = false;
    for (const recurso of EVENTOS_DEL_SERVIDOR) {
      fuente.addEventListener(recurso, () => this.actualizaciones.notificarExterno(recurso));
    }
    fuente.onopen = () => {
      if (yaSeAbrio) {
        EVENTOS_DEL_SERVIDOR.forEach((recurso) => this.actualizaciones.notificarExterno(recurso));
      }
      yaSeAbrio = true;
    };
  }

  private escucharAOtrasVentanas(): void {
    const canal = this.crearCanal();
    if (!canal) {
      return;
    }
    canal.onmessage = (evento) => {
      if (this.actualizaciones.esRecurso(evento.data)) {
        this.actualizaciones.notificarExterno(evento.data);
      }
    };
    this.actualizaciones.locales$.subscribe((recurso) => canal.postMessage(recurso));
  }
}
