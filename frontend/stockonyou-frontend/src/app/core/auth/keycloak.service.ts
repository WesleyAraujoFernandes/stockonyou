import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import Keycloak, { KeycloakProfile } from 'keycloak-js';
import { firstValueFrom } from 'rxjs';


interface KeycloakTokenResponse {
  access_token: string;
  refresh_token: string;
  id_token: string;
}

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

  isLoggedIn(): boolean {
    return !!this.keycloak.authenticated;
  }

  async getToken(): Promise<string | undefined> {
    if (this.keycloak.authenticated) {
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
      const response = await firstValueFrom(
        this.http.post<KeycloakTokenResponse>(tokenUrl, body.toString(), {
          headers: new HttpHeaders({ 'Content-Type': 'application/x-www-form-urlencoded' })
        })
      );

      this.keycloak.token = response.access_token;
      this.keycloak.refreshToken = response.refresh_token;
      this.keycloak.idToken = response.id_token;
      this.keycloak.authenticated = true;

      this.userProfile = await this.keycloak.loadUserProfile();

      return true;
    } catch (error) {
      console.error('Falha na autenticação via Keycloak', error);
      return false;
    }
  }

  async login(): Promise<void> {
    await this.keycloak.login();
  }

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
