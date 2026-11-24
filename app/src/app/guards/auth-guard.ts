import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth-service';

export const authGuard: CanActivateFn = (_route, state) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  return auth.autenticado() ? true : router.createUrlTree(['/ingresar'], { queryParams: { redirect: state.url } });
};

export const adminGuard: CanActivateFn = (_route, state) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (!auth.autenticado()) {
    return router.createUrlTree(['/ingresar'], { queryParams: { redirect: state.url } });
  }
  return auth.esAdmin() ? true : router.createUrlTree(['/']);
};

export const invitadoGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  return auth.autenticado() ? router.createUrlTree(['/']) : true;
};
