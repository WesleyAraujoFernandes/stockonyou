import { Component, inject, OnInit, signal } from '@angular/core';
import { KeycloakService } from '../../core/auth/keycloak.service';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-login',
  imports: [CommonModule, FormsModule],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login {
  private keycloakService = inject(KeycloakService);
  private router = inject(Router);

  username = '';
  password = '';
  errorMessage = signal<string>('');
  isLoading = signal<boolean>(false);

  async entrarComCredenciais(): Promise<void> {
    if (!this.username || !this.password) {
      this.errorMessage.set('Preencha o usuário e a senha.');
      return;
    }

    this.isLoading.set(true);
    this.errorMessage.set('');

    const sucesso = await this.keycloakService.loginComCredenciais(this.username, this.password);

    this.isLoading.set(false);

    if (sucesso) {
      this.router.navigate(['/produtos']);
    } else {
      this.errorMessage.set('Usuário ou senha inválidos.');
    }
  }

  // Mantido caso queira dar opção de usar o login redirecionado
  entrarRedirecionar(): void {
    this.keycloakService.login();
  }
}
