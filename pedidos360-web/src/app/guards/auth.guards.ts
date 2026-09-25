import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthHelperService } from '../services/auth-helper.service';

export const authGuard: CanActivateFn = () => {
  const auth = inject(AuthHelperService);
  const router = inject(Router);

  if (auth.isLoggedIn) return true;
  return router.parseUrl('/login');
};