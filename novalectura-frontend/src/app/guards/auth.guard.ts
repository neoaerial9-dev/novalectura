import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { LocalStorageService } from '../services/local-storage.service';

export const authGuard: CanActivateFn = () => {
  const localStorage = inject(LocalStorageService);
  const router = inject(Router);

  return localStorage.getToken()
    ? true
    : router.createUrlTree(['/login']);
};
