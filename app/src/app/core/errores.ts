import { HttpErrorResponse } from '@angular/common/http';

interface CuerpoError {
  detail?: string;
  errores?: Record<string, string>;
}

export function mensajeError(err: unknown, porDefecto = 'Ocurrió un error inesperado'): string {
  if (err instanceof HttpErrorResponse) {
    if (err.status === 0) {
      return 'No se pudo conectar con el servidor';
    }
    const cuerpo = err.error as CuerpoError | null;
    if (cuerpo?.errores && Object.keys(cuerpo.errores).length > 0) {
      return Object.values(cuerpo.errores).join('. ');
    }
    if (cuerpo?.detail) {
      return cuerpo.detail;
    }
    if (err.status === 403) {
      return 'No tienes permisos para realizar esta acción';
    }
  }
  return porDefecto;
}

export function destinoSeguro(destino: string | null | undefined, porDefecto = '/'): string {
  if (!destino || !destino.startsWith('/') || destino.startsWith('//') || destino.includes('\\')) {
    return porDefecto;
  }
  return destino;
}
