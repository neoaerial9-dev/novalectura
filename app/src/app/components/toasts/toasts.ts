import { Component, inject } from '@angular/core';
import { ToastService } from '../../services/toast-service';
import { Icono } from '../icono/icono';

@Component({
  selector: 'app-toasts',
  imports: [Icono],
  template: `
    <div class="toasts" aria-live="polite">
      @for (aviso of toast.avisos(); track aviso.id) {
        <div class="toast" [class.toast--error]="aviso.tipo === 'error'" [attr.role]="aviso.tipo === 'error' ? 'alert' : 'status'">
          <span>{{ aviso.texto }}</span>
          <button type="button" class="cerrar" aria-label="Cerrar aviso" (click)="toast.cerrar(aviso.id)">
            <app-icono nombre="cerrar" [tamano]="14" />
          </button>
        </div>
      }
    </div>
  `,
  styles: `
    .toasts {
      position: fixed;
      right: 20px;
      bottom: 20px;
      z-index: 100;
      display: flex;
      flex-direction: column;
      gap: 10px;
      max-width: min(380px, calc(100vw - 40px));
    }
    .toast {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 12px;
      background: var(--navy);
      color: #fff;
      border-radius: 12px;
      padding: 12px 14px;
      font-size: 0.875rem;
      box-shadow: 0 8px 24px rgb(30 45 74 / 28%);
    }
    .toast--error {
      background: var(--peligro);
    }
    @media (max-width: 520px) {
      .toasts {
        left: 12px;
        right: 12px;
        bottom: 12px;
        max-width: none;
      }
    }
    .cerrar {
      background: none;
      border: 0;
      color: inherit;
      cursor: pointer;
      opacity: 0.8;
    }
  `,
})
export class Toasts {
  protected readonly toast = inject(ToastService);
}
