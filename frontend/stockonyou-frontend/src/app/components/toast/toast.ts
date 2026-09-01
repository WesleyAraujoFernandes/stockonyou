import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { LucideCheckCircle, LucideX, LucideXCircle } from '@lucide/angular';
import { ToastService } from '../../core/services/toast.service';

@Component({
  selector: 'app-toast',
  imports: [CommonModule, LucideCheckCircle, LucideXCircle, LucideX],
  templateUrl: './toast.html',
  styleUrl: './toast.css',
})
export class Toast {
  protected readonly toastService = inject(ToastService);
}
