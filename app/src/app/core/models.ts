export type Rol = 'CLIENTE' | 'ADMINISTRADOR';

export type CondicionLibro = 'COMO_NUEVO' | 'MUY_BUENO' | 'BUENO' | 'ACEPTABLE';

export const CONDICIONES: readonly CondicionLibro[] = ['COMO_NUEVO', 'MUY_BUENO', 'BUENO', 'ACEPTABLE'];

export const CONDICION_ETIQUETA: Record<CondicionLibro, string> = {
  COMO_NUEVO: 'Como nuevo',
  MUY_BUENO: 'Muy bueno',
  BUENO: 'Bueno',
  ACEPTABLE: 'Aceptable',
};

export interface Usuario {
  id: number;
  nombres: string;
  apellidos: string;
  correo: string;
  rol: Rol;
  telefono: string | null;
  direccionEnvio: string | null;
  activo: boolean;
  fechaRegistro: string;
}

export interface AuthResponse {
  token: string;
  tipo: string;
  usuario: Usuario;
}

export interface LoginRequest {
  correo: string;
  contrasena: string;
}

export interface RegistroRequest extends LoginRequest {
  nombres: string;
  apellidos: string;
}

export interface PerfilRequest {
  nombres: string;
  apellidos: string;
  telefono: string | null;
  direccionEnvio: string | null;
}

export interface CambioContrasenaRequest {
  actual: string;
  nueva: string;
}

export interface Categoria {
  id: number;
  nombre: string;
  descripcion: string | null;
}

export interface CategoriaRequest {
  nombre: string;
  descripcion: string | null;
}

export interface Libro {
  id: number;
  titulo: string;
  autor: string;
  editorial: string | null;
  isbn: string | null;
  anioPublicacion: number | null;
  descripcion: string | null;
  precio: number;
  stock: number;
  imagen: string | null;
  condicion: CondicionLibro;
  categoria: Categoria;
  vendedorId: number;
  vendedorNombre: string;
}

export interface LibroRequest {
  titulo: string;
  autor: string;
  editorial: string | null;
  isbn: string | null;
  anioPublicacion: number | null;
  descripcion: string | null;
  precio: number;
  stock: number;
  imagen: string | null;
  condicion: CondicionLibro;
  categoriaId: number;
}

export interface Pagina<T> {
  content: T[];
  page: { size: number; number: number; totalElements: number; totalPages: number };
}

export interface ItemCarrito {
  libroId: number;
  titulo: string;
  imagen: string | null;
  precioUnitario: number;
  cantidad: number;
  subtotal: number;
  stockDisponible: number;
  disponible: boolean;
}

export interface Carrito {
  items: ItemCarrito[];
  total: number;
}

export interface DetallePedido {
  libroId: number;
  titulo: string;
  cantidad: number;
  precioUnitario: number;
  subtotal: number;
}

export interface Pedido {
  id: number;
  fechaPedido: string;
  total: number;
  estado: string;
  cliente: string;
  correoCliente: string;
  detalles: DetallePedido[];
}

export interface Venta {
  pedidoId: number;
  fechaPedido: string;
  libroId: number;
  titulo: string;
  cantidad: number;
  precioUnitario: number;
  subtotal: number;
  comprador: string;
  correoComprador: string;
}
