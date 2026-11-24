import { Pipe, PipeTransform } from '@angular/core';
import { CONDICION_ETIQUETA, CondicionLibro } from '../core/models';

@Pipe({ name: 'condicion' })
export class CondicionPipe implements PipeTransform {
  transform(valor: CondicionLibro | null | undefined): string {
    return valor ? CONDICION_ETIQUETA[valor] : '';
  }
}
