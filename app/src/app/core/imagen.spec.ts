import { environment } from '../../environments/environment';
import { IMAGEN_PLACEHOLDER, resolverImagen, validarImagen } from './imagen';

describe('resolverImagen', () => {
  it('usa la portada de reemplazo si no hay imagen', () => {
    expect(resolverImagen(null)).toBe(IMAGEN_PLACEHOLDER);
    expect(resolverImagen('')).toBe(IMAGEN_PLACEHOLDER);
  });

  it('sirve las rutas propias desde el origen de la API', () => {
    expect(resolverImagen('/api/imagenes/abc.png')).toBe(`${environment.origenApi}/api/imagenes/abc.png`);
  });

  it('respeta las URLs completas', () => {
    expect(resolverImagen('https://sitio.com/foto.jpg')).toBe('https://sitio.com/foto.jpg');
  });
});

describe('validarImagen', () => {
  const archivo = (tipo: string, bytes = 10) => new File([new Uint8Array(bytes)], 'x', { type: tipo });

  it('acepta JPG, PNG y WEBP pequeños', () => {
    expect(validarImagen(archivo('image/jpeg'))).toBeNull();
    expect(validarImagen(archivo('image/png'))).toBeNull();
    expect(validarImagen(archivo('image/webp'))).toBeNull();
  });

  it('rechaza otros tipos', () => {
    expect(validarImagen(archivo('application/pdf'))).toContain('JPG, PNG o WEBP');
  });

  it('rechaza archivos de más de 2 MB', () => {
    expect(validarImagen(archivo('image/png', 2 * 1024 * 1024 + 1))).toContain('2 MB');
  });
});
