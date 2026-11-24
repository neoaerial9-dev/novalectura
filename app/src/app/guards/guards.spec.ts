import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, CanActivateFn, Router, RouterStateSnapshot, UrlTree, provideRouter } from '@angular/router';
import { AuthService } from '../services/auth-service';
import { adminGuard, authGuard, invitadoGuard } from './auth-guard';

function ejecutar(guard: CanActivateFn, autenticado: boolean, esAdmin: boolean, url = '/carrito'): boolean | UrlTree {
  TestBed.configureTestingModule({
    providers: [
      provideRouter([]),
      { provide: AuthService, useValue: { autenticado: signal(autenticado), esAdmin: signal(esAdmin) } },
    ],
  });
  return TestBed.runInInjectionContext(() =>
    guard({} as ActivatedRouteSnapshot, { url } as RouterStateSnapshot),
  ) as boolean | UrlTree;
}

describe('authGuard', () => {
  it('deja pasar a un usuario autenticado', () => {
    expect(ejecutar(authGuard, true, false)).toBe(true);
  });

  it('redirige a /ingresar guardando la ruta de destino', () => {
    const resultado = ejecutar(authGuard, false, false, '/carrito') as UrlTree;
    expect(TestBed.inject(Router).serializeUrl(resultado)).toBe('/ingresar?redirect=%2Fcarrito');
  });
});

describe('adminGuard', () => {
  it('deja pasar al administrador', () => {
    expect(ejecutar(adminGuard, true, true, '/admin/usuarios')).toBe(true);
  });

  it('manda al inicio a un cliente', () => {
    const resultado = ejecutar(adminGuard, true, false, '/admin/usuarios') as UrlTree;
    expect(TestBed.inject(Router).serializeUrl(resultado)).toBe('/');
  });

  it('manda a iniciar sesión a un visitante', () => {
    const resultado = ejecutar(adminGuard, false, false, '/admin/usuarios') as UrlTree;
    expect(TestBed.inject(Router).serializeUrl(resultado)).toContain('/ingresar');
  });
});

describe('invitadoGuard', () => {
  it('permite entrar a un visitante', () => {
    expect(ejecutar(invitadoGuard, false, false, '/ingresar')).toBe(true);
  });

  it('envía al inicio a quien ya inició sesión', () => {
    const resultado = ejecutar(invitadoGuard, true, false, '/ingresar') as UrlTree;
    expect(TestBed.inject(Router).serializeUrl(resultado)).toBe('/');
  });
});
