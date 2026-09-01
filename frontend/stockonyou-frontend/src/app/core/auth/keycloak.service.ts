import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import Keycloak, { KeycloakProfile } from 'keycloak-js';
import { firstValueFrom } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class KeycloakService {
  private http = inject(HttpClient)
  private keycloak = new Keycloak({
    url: 'http://localhost:8091',
    realm: 'estoque-realm',
    clientId: 'estoque-app',
  });

  private userProfile: KeycloakProfile | null = null;

  async init(): Promise<boolean> {
    const authenticated = await this.keycloak.init({
      onLoad: 'check-sso',
      checkLoginIframe: false,
    });

    if (authenticated) {
      this.userProfile = await this.keycloak.loadUserProfile();
    }

    return authenticated;
  }

  // --- MÉTODOS ADICIONADOS PARA RESOLVER OS ERROS ---

  /**
   * Verifica se o usuário está autenticado
   */
  isLoggedIn(): boolean {
    return !!this.keycloak.authenticated;
  }

  /**
   * Retorna o token JWT atual ou atualiza caso esteja prestes a expirar
   */
  async getToken(): Promise<string | undefined> {
    if (this.keycloak.authenticated) {
      // Atualiza o token se ele for expirar nos próximos 30 segundos
      try {
        await this.keycloak.updateToken(30);
      } catch (error) {
        console.error('Erro ao atualizar token do Keycloak:', error);
      }
      return this.keycloak.token;
    }
    return undefined;
  }

  async loginComCredenciais(username: string, password: string): Promise<boolean> {
    const tokenUrl = 'http://localhost:8091/realms/estoque-realm/protocol/openid-connect/token';
    const body = new HttpParams()
      .set('client_id', 'estoque-app')
      .set('grant_type', 'password')
      .set('username', username)
      .set('password', password);

    try {
      const response: any = await firstValueFrom(
        this.http.post(tokenUrl, body.toString(), {
          headers: new HttpHeaders({ 'Content-Type': 'application/x-www-form-urlencoded' })
        })
      );

      // Atribui os tokens diretamente à instância do keycloak-js
      this.keycloak.token = response.access_token;
      this.keycloak.refreshToken = response.refresh_token;
      this.keycloak.idToken = response.id_token;
      this.keycloak.authenticated = true;

      // Carrega o perfil do usuário recém-logado
      this.userProfile = await this.keycloak.loadUserProfile();

      return true;
    } catch (error) {
      console.error('Falha na autenticação via Keycloak', error);
      return false;
    }
  }

  /**
   * Redireciona o usuário para a tela de login do Keycloak
   */
  async login(): Promise<void> {
    await this.keycloak.login();
  }

  // --- MÉTODOS DE PERFIL E LOGOUT ---

  getUserProfile(): KeycloakProfile | null {
    return this.userProfile;
  }

  getUserDisplayName(): string {
    if (!this.userProfile) return 'Usuário';

    if (this.userProfile.firstName || this.userProfile.lastName) {
      return `${this.userProfile.firstName ?? ''} ${this.userProfile.lastName ?? ''}`.trim();
    }

    return this.userProfile.username ?? this.userProfile.email ?? 'Usuário';
  }

async logout(): Promise<void> {
    await this.keycloak.logout({
      redirectUri: `${window.location.origin}/login`,
    });
  }
}
