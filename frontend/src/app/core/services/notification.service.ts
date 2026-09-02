import { Injectable, signal } from '@angular/core';

export interface Notification {
  type: 'success' | 'error';
  message: string;
}

@Injectable({ providedIn: 'root' })
export class NotificationService {

  readonly notification = signal<Notification | null>(null);
  private timeoutHandle: ReturnType<typeof setTimeout> | null = null;

  showSuccess(message: string): void {
    this.show({ type: 'success', message });
  }

  showError(message: string): void {
    this.show({ type: 'error', message });
  }

  clear(): void {
    this.notification.set(null);
  }

  private show(notification: Notification): void {
    this.notification.set(notification);
    if (this.timeoutHandle) {
      clearTimeout(this.timeoutHandle);
    }
    this.timeoutHandle = setTimeout(() => this.notification.set(null), 5000);
  }
}
