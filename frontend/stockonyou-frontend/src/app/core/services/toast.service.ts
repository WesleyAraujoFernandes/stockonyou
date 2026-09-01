import { Injectable, signal } from '@angular/core';

export interface ToastData {
  mensagem: string;
  tipo: 'success' | 'error';
}

@Injectable({
  providedIn: 'root',
})
export class ToastService {
  private readonly toastState = signal<ToastData | null>(null);
  readonly toast = this.toastState.asReadonly();


  sucesso(mensagem: string): void {
    this.exibir(mensagem, 'success');
  }

  erro(mensagem: string): void {
    this.exibir(mensagem, 'error');
  }

  private exibir(mensagem: string, tipo: 'success' | 'error'): void {
    this.toastState.set({ mensagem, tipo });
    setTimeout(() => {
      this.toastState.set(null);
    }, 3000)
  }

  fechar(): void {
    this.toastState.set(null);
  }
}
