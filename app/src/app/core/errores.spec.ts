import { HttpErrorResponse } from '@angular/common/http';
import { destinoSeguro, mensajeError } from './errores';

describe('mensajeError', () => {
  it('usa el detalle del ProblemDetail', () => {
    const err = new HttpErrorResponse({ status: 409, error: { detail: 'El correo ya está registrado' } });
    expect(mensajeError(err)).toBe('El correo ya está registrado');
  });

  it('une los errores de validación por campo', () => {
    const err = new HttpErrorResponse({
      status: 400,
      error: { detail: 'Los datos enviados no son válidos', errores: { correo: 'El correo no es válido', contrasena: 'Muy corta' } },
    });
    expect(mensajeError(err)).toBe('El correo no es válido. Muy corta');
  });

  it('avisa cuando no hay conexión con el servidor', () => {
    expect(mensajeError(new HttpErrorResponse({ status: 0 }))).toBe('No se pudo conectar con el servidor');
  });

  it('devuelve el mensaje por defecto para errores desconocidos', () => {
    expect(mensajeError(new Error('x'), 'Falló')).toBe('Falló');
  });
});

describe('destinoSeguro', () => {
  it('acepta rutas internas', () => {
    expect(destinoSeguro('/carrito')).toBe('/carrito');
  });

  it('rechaza URLs externas o protocolo relativo', () => {
    expect(destinoSeguro('https://malo.com')).toBe('/');
    expect(destinoSeguro('//malo.com')).toBe('/');
    expect(destinoSeguro('/\\malo.com')).toBe('/');
  });

  it('usa el valor por defecto si no hay destino', () => {
    expect(destinoSeguro(undefined)).toBe('/');
    expect(destinoSeguro(null, '/perfil')).toBe('/perfil');
  });
});
