import { Injectable, signal } from '@angular/core';

export interface ToastData {
  mensagem: string;
  tipo: 'success' | 'error' | 'info';
}

@Injectable({
  providedIn: 'root',
})
export class ToastService {
  private readonly toastState = signal<ToastData | null>(null);
  readonly toast = this.toastState.asReadonly();
  private timeoutId: ReturnType<typeof setTimeout> | null = null;


  sucesso(mensagem: string): void {
    this.exibir(mensagem, 'success');
  }

  erro(mensagem: string): void {
    console.log('TOAST.erro() CHAMADO:', mensagem)
    this.exibir(mensagem, 'error');
    console.log('TOAST APÓS exibir():', this.toast())
  }

  info(mensagem: string): void {
    this.exibir(mensagem, 'info');
  }

  private exibir(
    mensagem: string,
    tipo: 'success' | 'error' | 'info'
  ): void {
    if (this.timeoutId !== null) {
      clearTimeout(this.timeoutId);
    }
    this.toastState.set({ mensagem, tipo });
    this.timeoutId = setTimeout(() => {
      this.toastState.set(null);
      this.timeoutId = null
    }, 3000);
  }

  fechar(): void {
    if (this.timeoutId !== null) {
      clearTimeout(this.timeoutId);
      this.timeoutId = null;
    }
    this.toastState.set(null);
  }
}
