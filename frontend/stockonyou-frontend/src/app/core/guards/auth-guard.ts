import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { KeycloakService } from '../auth/keycloak.service';

export const authGuard: CanActivateFn = async (route, state) => {
  const keycloakService = inject(KeycloakService);
  const router = inject(Router);

  if (keycloakService.isLoggedIn()) {
    return true;
  }

  try {
    const token = await keycloakService.getToken();
    if (token && keycloakService.isLoggedIn()) {
      return true;
    }
  } catch (error) {
    console.warn('Sessão ativa não encontrada no acesso direto por URL.');
  }


  router.navigate(['/login']);
  return false;
};
