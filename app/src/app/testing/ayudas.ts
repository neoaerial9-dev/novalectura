import { ComponentFixture } from '@angular/core/testing';
import { Libro, Pedido, Usuario, Venta } from '../core/models';

export const usuarioPrueba: Usuario = {
  id: 1,
  nombres: 'Ana',
  apellidos: 'Paz',
  correo: 'ana@mail.com',
  rol: 'CLIENTE',
  telefono: null,
  direccionEnvio: null,
  activo: true,
  fechaRegistro: '2026-11-14T10:00:00',
};

export function iniciarSesion(usuario: Usuario = usuarioPrueba): void {
  localStorage.setItem('nl.token', 'TOKEN');
  localStorage.setItem('nl.usuario', JSON.stringify(usuario));
}

export function libroPrueba(id: number, extra: Partial<Libro> = {}): Libro {
  return {
    id,
    titulo: `Libro ${id}`,
    autor: 'Autor',
    editorial: null,
    isbn: null,
    anioPublicacion: null,
    descripcion: null,
    precio: 20,
    stock: 3,
    imagen: null,
    condicion: 'BUENO',
    categoria: { id: 1, nombre: 'Ficción', descripcion: null },
    vendedorId: 99,
    vendedorNombre: 'Vendedor Uno',
    ...extra,
  };
}

export function paginaDe<T>(content: T[], page = 0, totalPages = 1): { content: T[]; page: { size: number; number: number; totalElements: number; totalPages: number } } {
  return { content, page: { size: 12, number: page, totalElements: content.length, totalPages } };
}

export function escribir(fixture: ComponentFixture<unknown>, selector: string, valor: string): void {
  const campo = (fixture.nativeElement as HTMLElement).querySelector<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>(selector);
  if (!campo) {
    throw new Error(`No existe el campo ${selector}`);
  }
  campo.value = valor;
  campo.dispatchEvent(new Event(campo instanceof HTMLSelectElement ? 'change' : 'input'));
}

export function enviarFormulario(fixture: ComponentFixture<unknown>, selector = 'form'): void {
  (fixture.nativeElement as HTMLElement).querySelector(selector)?.dispatchEvent(new Event('submit'));
}

export function pedidoPrueba(id: number, extra: Partial<Pedido> = {}): Pedido {
  return {
    id,
    fechaPedido: '2026-11-14T10:00:00',
    total: 40,
    estado: 'REGISTRADO',
    cliente: 'Ana Paz',
    correoCliente: 'ana@mail.com',
    detalles: [{ libroId: 7, titulo: 'Dune', cantidad: 2, precioUnitario: 20, subtotal: 40 }],
    ...extra,
  };
}

export function ventaPrueba(pedidoId: number, extra: Partial<Venta> = {}): Venta {
  return {
    pedidoId,
    fechaPedido: '2026-11-14T10:00:00',
    libroId: 7,
    titulo: 'Dune',
    cantidad: 1,
    precioUnitario: 20,
    subtotal: 20,
    comprador: 'Luis Gil',
    correoComprador: 'luis@mail.com',
    ...extra,
  };
}
