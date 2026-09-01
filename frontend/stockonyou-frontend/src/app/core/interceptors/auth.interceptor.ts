import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { from, switchMap } from 'rxjs';
import { KeycloakService } from '../auth/keycloak.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const keycloakService = inject(KeycloakService);

  // Ignora chamadas para arquivos estáticos locais
  if (
    req.url.startsWith('./assets') ||
    req.url.startsWith('/assets') ||
    req.url.startsWith('./public') ||
    req.url.startsWith('/public')
  ) {
    return next(req);
  }

  return from(keycloakService.getToken()).pipe(
    switchMap((token) => {
      if (token) {
        const clonedRequest = req.clone({
          setHeaders: {
            Authorization: `Bearer ${token}`,
          },
        });
        return next(clonedRequest);
      }
      return next(req);
    })
  );
};
