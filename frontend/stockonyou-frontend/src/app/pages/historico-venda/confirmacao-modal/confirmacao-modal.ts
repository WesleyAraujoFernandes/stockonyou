import { CommonModule } from '@angular/common';
import { Component, input, output } from '@angular/core';


@Component({
  selector: 'app-confirmacao-modal',
  imports: [CommonModule],
  templateUrl: './confirmacao-modal.html',
  styleUrl: './confirmacao-modal.css',
})
export class ConfirmacaoModal {
  readonly titulo = input('Confirmar ação')
  readonly mensagem = input('')
  readonly textoConfirmar = input('Confirmar')
  readonly textoCancelar = input ('Cancelar')
  readonly variante = input<'danger' | 'success'>('danger')
  readonly desabilitado = input(false);

  readonly confirmado = output<void>();
  readonly cancelado = output<void>();
}
