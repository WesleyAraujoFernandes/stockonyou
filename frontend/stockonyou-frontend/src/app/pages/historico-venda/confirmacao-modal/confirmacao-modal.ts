import { Component, input, output } from '@angular/core';

@Component({
  selector: 'app-confirmacao-modal',
  imports: [],
  templateUrl: './confirmacao-modal.html',
  styleUrl: './confirmacao-modal.css',
})
export class ConfirmacaoModal {
  readonly titulo = input('Confirmar ação')
  readonly mensagem = input('')
  readonly textConfirmar = input('Confirmar')
  readonly textoCancelar = input ('Cancelar')

  readonly confirmado = output<void>();
  readonly cancelado = output<void>();
}
