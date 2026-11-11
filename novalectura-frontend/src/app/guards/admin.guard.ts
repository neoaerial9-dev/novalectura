import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { LocalStorageService } from '../services/local-storage.service';

export const adminGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const localStorage = inject(LocalStorageService);
  const router = inject(Router);

  if (!localStorage.getToken()) {
    return router.createUrlTree(['/login']);
  }

  return authService.isAdmin()
    ? true
    : router.createUrlTree(['/catalogo']);
};
