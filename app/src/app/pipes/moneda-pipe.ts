import { formatNumber } from '@angular/common';
import { Pipe, PipeTransform } from '@angular/core';
import { MONEDA } from '../core/config';

@Pipe({ name: 'moneda' })
export class MonedaPipe implements PipeTransform {
  transform(valor: number | null | undefined): string {
    if (valor == null) {
      return '';
    }
    return `${MONEDA.simbolo}${formatNumber(valor, 'en-US', '1.2-2')}`;
  }
}
