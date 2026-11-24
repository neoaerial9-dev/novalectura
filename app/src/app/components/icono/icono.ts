import { Component, computed, input } from '@angular/core';

const ICONOS: Record<string, string[]> = {
  buscar: ['M19 11a8 8 0 1 1-16 0 8 8 0 0 1 16 0Z', 'm21 21-4.3-4.3'],
  carrito: [
    'M9 21a1 1 0 1 1-2 0 1 1 0 0 1 2 0Z',
    'M20 21a1 1 0 1 1-2 0 1 1 0 0 1 2 0Z',
    'M2.05 2.05h2l2.66 12.42a2 2 0 0 0 2 1.58h9.78a2 2 0 0 0 1.95-1.57l1.65-7.43H5.12',
  ],
  libro: ['M4 19.5v-15A2.5 2.5 0 0 1 6.5 2H19a1 1 0 0 1 1 1v18a1 1 0 0 1-1 1H6.5a1 1 0 0 1 0-5H20'],
  usuario: ['M19 21v-2a4 4 0 0 0-4-4H9a4 4 0 0 0-4 4v2', 'M16 7a4 4 0 1 1-8 0 4 4 0 0 1 8 0Z'],
  salir: ['M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4', 'm16 17 5-5-5-5', 'M21 12H9'],
  mas: ['M5 12h14', 'M12 5v14'],
  menos: ['M5 12h14'],
  cerrar: ['M18 6 6 18', 'm6 6 12 12'],
  papelera: ['M3 6h18', 'M19 6v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6', 'M8 6V4c0-1 1-2 2-2h4c1 0 2 1 2 2v2'],
  lapiz: ['M17 3a2.85 2.83 0 1 1 4 4L7.5 20.5 2 22l1.5-5.5Z'],
  izquierda: ['m15 18-6-6 6-6'],
  derecha: ['m9 18 6-6-6-6'],
  flecha: ['M19 12H5', 'm12 19-7-7 7-7'],
  menu: ['M4 6h16', 'M4 12h16', 'M4 18h16'],
};

@Component({
  selector: 'app-icono',
  host: { style: 'display: inline-flex; line-height: 0' },
  template: `<svg
    [attr.width]="tamano()"
    [attr.height]="tamano()"
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    stroke-width="2"
    stroke-linecap="round"
    stroke-linejoin="round"
    aria-hidden="true"
  >
    @for (d of trazos(); track $index) {
      <path [attr.d]="d" />
    }
  </svg>`,
})
export class Icono {
  readonly nombre = input.required<string>();
  readonly tamano = input(18);
  protected readonly trazos = computed(() => ICONOS[this.nombre()] ?? []);
}
