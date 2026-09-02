import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

/** Allows CUSTOMER only (booking/search screens are customer-facing). */
export const customerGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isLoggedIn() && authService.isCustomer()) {
    return true;
  }

  router.navigate(['/dashboard']);
  return false;
};
