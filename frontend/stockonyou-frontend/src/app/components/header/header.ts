import { Component, inject, OnInit, signal } from '@angular/core';
import { KeycloakService } from '../../core/auth/keycloak.service';

@Component({
  selector: 'app-header',
  imports: [],
  templateUrl: './header.html',
  styleUrl: './header.css',
})
export class Header implements OnInit {
  private keycloakService = inject(KeycloakService);

  // Signals para gerenciamento reativo do perfil
  displayName = signal<string>('Carregando...');
  email = signal<string>('');
  userInitials = signal<string>('U');

  ngOnInit(): void {
    const profile = this.keycloakService.getUserProfile();
    const name = this.keycloakService.getUserDisplayName();

    this.displayName.set(name);
    this.email.set(profile?.email ?? '');

    // Extrai a inicial do nome para o avatar
    if (name) {
      this.userInitials.set(name.charAt(0).toUpperCase());
    }
  }

  logout(): void {
    this.keycloakService.logout();
  }
}
