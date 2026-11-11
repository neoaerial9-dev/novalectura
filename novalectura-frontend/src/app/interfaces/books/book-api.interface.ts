export interface BookCategory {
  idCategoria: number;
  nombre: string;
  descripcion: string | null;
}

export interface ManagedBook {
  idLibro: number;
  titulo: string;
  autor: string;
  precio: number;
  stock: number;
  descripcion: string | null;
  imagen: string | null;
  condicion: BookCondition;
  fechaPublicacion: string;
  categoria: BookCategory;
  vendedor: {
    idUsuario: number;
    nombreCompleto: string;
  };
}

export type BookCondition = 'COMO_NUEVO' | 'MUY_BUENO' | 'BUENO' | 'ACEPTABLE';

export interface BookRequest {
  titulo: string;
  autor: string;
  precio: number;
  stock: number;
  descripcion: string;
  imagen: string;
  condicion: BookCondition;
  idCategoria: number;
}

export interface BookPage {
  contenido: ManagedBook[];
  pagina: number;
  tamano: number;
  totalElementos: number;
  totalPaginas: number;
}

export interface CartItemRequest {
  idLibro: number;
  cantidad: number;
}

export interface CartResponse {
  idCarrito: number;
  total: number;
  detalles: CartDetail[];
}

export interface CartDetail {
  idDetalle: number;
  idLibro: number;
  titulo: string;
  imagen: string | null;
  stockDisponible: number;
  cantidad: number;
  precioUnitario: number;
  subtotal: number;
}
