import { Component, input, output } from '@angular/core';

@Component({
  selector: 'app-confirm-dialog',
  imports: [],
  templateUrl: './confirm-dialog.html',
  styleUrl: './confirm-dialog.css',
})
export class ConfirmDialog {
  open = input(false);
  title = input('Confirmação')
  message = input('')
  confirmText = input('Confirmar')
  cancelText = input ('Cancelar')

  confirmed = output<void>();
  cancelled = output<void>();

  confirm(): void {
    this.confirmed.emit()
  }

  cancel(): void {
    this.cancelled.emit();
  }

}
