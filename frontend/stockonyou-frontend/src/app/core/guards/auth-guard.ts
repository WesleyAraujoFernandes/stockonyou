import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { KeycloakService } from '../auth/keycloak.service'; // Ajuste o caminho se necessário

export const authGuard: CanActivateFn = async (route, state) => {
  const keycloakService = inject(KeycloakService);
  const router = inject(Router);

  // 1. Se o Keycloak já concluiu a inicialização rápida e detectou o login, libera direto
  if (keycloakService.isLoggedIn()) {
    return true;
  }

  // 2. CORREÇÃO CRUCIAL: Em URLs diretas, força uma revalidação assíncrona do Token
  // antes de tomar a decisão de expulsar o usuário
  try {
    const token = await keycloakService.getToken();
    if (token && keycloakService.isLoggedIn()) {
      return true;
    }
  } catch (error) {
    console.warn('Sessão ativa não encontrada no acesso direto por URL.');
  }

  // 3. Se mesmo esperando o Keycloak ele continuar deslogado, manda para o login
  router.navigate(['/login']);
  return false;
};
