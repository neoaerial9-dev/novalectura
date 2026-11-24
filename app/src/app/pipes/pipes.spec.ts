import { CondicionPipe } from './condicion-pipe';
import { MonedaPipe } from './moneda-pipe';

describe('MonedaPipe', () => {
  const pipe = new MonedaPipe();

  it('formatea con símbolo y dos decimales', () => {
    expect(pipe.transform(18.99)).toBe('S/ 18.99');
    expect(pipe.transform(1250)).toBe('S/ 1,250.00');
  });

  it('devuelve vacío si no hay valor', () => {
    expect(pipe.transform(null)).toBe('');
  });
});

describe('CondicionPipe', () => {
  const pipe = new CondicionPipe();

  it('traduce la condición a una etiqueta legible', () => {
    expect(pipe.transform('COMO_NUEVO')).toBe('Como nuevo');
    expect(pipe.transform('ACEPTABLE')).toBe('Aceptable');
  });
});
