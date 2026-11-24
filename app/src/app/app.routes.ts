import { Routes } from '@angular/router';
import { adminGuard, authGuard, invitadoGuard } from './guards/auth-guard';

export const routes: Routes = [
  {
    path: '',
    title: 'Tienda de Libros',
    data: { hero: true },
    loadComponent: () => import('./pages/catalogo/catalogo').then((m) => m.Catalogo),
  },
  {
    path: 'catalogo',
    title: 'Catálogo · Tienda de Libros',
    loadComponent: () => import('./pages/catalogo/catalogo').then((m) => m.Catalogo),
  },
  {
    path: 'ingresar',
    title: 'Ingresar · Tienda de Libros',
    canActivate: [invitadoGuard],
    loadComponent: () => import('./pages/login/login').then((m) => m.Login),
  },
  {
    path: 'registro',
    title: 'Crear cuenta · Tienda de Libros',
    canActivate: [invitadoGuard],
    loadComponent: () => import('./pages/registro/registro').then((m) => m.Registro),
  },
  {
    path: 'carrito',
    title: 'Carrito · Tienda de Libros',
    canActivate: [authGuard],
    loadComponent: () => import('./pages/mi-carrito/mi-carrito').then((m) => m.MiCarrito),
  },
  {
    path: 'mis-pedidos',
    title: 'Mis pedidos · Tienda de Libros',
    canActivate: [authGuard],
    loadComponent: () => import('./pages/mis-pedidos/mis-pedidos').then((m) => m.MisPedidos),
  },
  {
    path: 'perfil',
    title: 'Mi perfil · Tienda de Libros',
    canActivate: [authGuard],
    loadComponent: () => import('./pages/mi-perfil/mi-perfil').then((m) => m.MiPerfil),
  },
  {
    path: 'vender',
    title: 'Mis libros · Tienda de Libros',
    canActivate: [authGuard],
    loadComponent: () => import('./pages/mis-libros/mis-libros').then((m) => m.MisLibros),
  },
  {
    path: 'vender/nuevo',
    title: 'Publicar libro · Tienda de Libros',
    canActivate: [authGuard],
    loadComponent: () => import('./pages/formulario-libro/formulario-libro').then((m) => m.FormularioLibro),
  },
  {
    path: 'vender/:id/editar',
    title: 'Editar libro · Tienda de Libros',
    canActivate: [authGuard],
    loadComponent: () => import('./pages/formulario-libro/formulario-libro').then((m) => m.FormularioLibro),
  },
  {
    path: 'mis-ventas',
    title: 'Mis ventas · Tienda de Libros',
    canActivate: [authGuard],
    loadComponent: () => import('./pages/mis-ventas/mis-ventas').then((m) => m.MisVentas),
  },
  {
    path: 'admin/usuarios',
    title: 'Usuarios · Tienda de Libros',
    canActivate: [adminGuard],
    loadComponent: () => import('./pages/admin-usuarios/admin-usuarios').then((m) => m.AdminUsuarios),
  },
  {
    path: 'admin/pedidos',
    title: 'Pedidos · Tienda de Libros',
    canActivate: [adminGuard],
    loadComponent: () => import('./pages/admin-pedidos/admin-pedidos').then((m) => m.AdminPedidos),
  },
  {
    path: 'admin/categorias',
    title: 'Categorías · Tienda de Libros',
    canActivate: [adminGuard],
    loadComponent: () => import('./pages/admin-categorias/admin-categorias').then((m) => m.AdminCategorias),
  },
  {
    path: '**',
    title: 'No encontrado · Tienda de Libros',
    loadComponent: () => import('./pages/no-encontrado/no-encontrado').then((m) => m.NoEncontrado),
  },
];
