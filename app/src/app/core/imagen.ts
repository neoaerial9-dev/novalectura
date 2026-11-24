import { environment } from '../../environments/environment';

const SVG = `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 400 300">
<rect width="400" height="300" fill="#efe6da"/>
<g fill="none" stroke="#b37d62" stroke-width="6" stroke-linecap="round" stroke-linejoin="round" opacity=".85">
<path d="M150 90v120c0-12 10-20 22-20h78V82h-78c-12 0-22 8-22 8Z"/>
<path d="M250 82v108"/></g></svg>`;

export const IMAGEN_PLACEHOLDER = 'data:image/svg+xml;utf8,' + encodeURIComponent(SVG);

export const TIPOS_IMAGEN = ['image/jpeg', 'image/png', 'image/webp'] as const;
export const MAX_BYTES_IMAGEN = 2 * 1024 * 1024;

export function resolverImagen(ruta: string | null | undefined): string {
  if (!ruta) {
    return IMAGEN_PLACEHOLDER;
  }
  if (/^(https?:|data:|blob:)/i.test(ruta)) {
    return ruta;
  }
  return environment.origenApi + ruta;
}

export function validarImagen(archivo: File): string | null {
  if (!(TIPOS_IMAGEN as readonly string[]).includes(archivo.type)) {
    return 'Solo se permiten imágenes JPG, PNG o WEBP';
  }
  if (archivo.size > MAX_BYTES_IMAGEN) {
    return 'La imagen no puede superar los 2 MB';
  }
  return null;
}
