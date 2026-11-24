import { Directive, computed, input, linkedSignal } from '@angular/core';
import { IMAGEN_PLACEHOLDER, resolverImagen } from '../core/imagen';

@Directive({
  selector: 'img[appImagenLibro]',
  host: {
    '[src]': 'src()',
    '(error)': 'fallo.set(true)',
  },
})
export class ImagenLibro {
  readonly appImagenLibro = input<string | null | undefined>();
  protected readonly fallo = linkedSignal({ source: this.appImagenLibro, computation: () => false });
  protected readonly src = computed(() => (this.fallo() ? IMAGEN_PLACEHOLDER : resolverImagen(this.appImagenLibro())));
}
