import { Service, signal } from '@angular/core';

export interface Aviso {
  id: number;
  tipo: 'ok' | 'error';
  texto: string;
}

@Service()
export class ToastService {
  private siguiente = 1;
  readonly avisos = signal<Aviso[]>([]);

  exito(texto: string): void {
    this.mostrar('ok', texto);
  }

  error(texto: string): void {
    this.mostrar('error', texto);
  }

  cerrar(id: number): void {
    this.avisos.update((lista) => lista.filter((a) => a.id !== id));
  }

  private mostrar(tipo: Aviso['tipo'], texto: string): void {
    const id = this.siguiente++;
    this.avisos.update((lista) => [...lista, { id, tipo, texto }]);
    setTimeout(() => this.cerrar(id), 4500);
  }
}
