import { Component, ElementRef, inject, viewChild } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router, RouterOutlet } from '@angular/router';
import { filter } from 'rxjs';
import { Footer } from './components/footer/footer';
import { Navbar } from './components/navbar/navbar';
import { Toasts } from './components/toasts/toasts';
import { SincronizacionService } from './services/sincronizacion-service';

@Component({
  imports: [RouterOutlet, Navbar, Footer, Toasts],
  selector: 'app-root',
  template: `
    <a class="saltar" href="#contenido" (click)="irAlContenido($event)">Saltar al contenido</a>
    <app-navbar />
    <main id="contenido" #principal class="principal" tabindex="-1"><router-outlet /></main>
    <app-footer />
    <app-toasts />
  `,
  styles: `
    :host {
      display: flex;
      flex-direction: column;
      min-height: 100vh;
    }
    .principal {
      flex: 1;
      outline: none;
    }
    .saltar {
      position: absolute;
      left: 12px;
      top: -100px;
      z-index: 200;
      background: #fff;
      color: var(--navy);
      padding: 10px 16px;
      border-radius: 8px;
      font-weight: 600;
    }
    .saltar:focus {
      top: 12px;
    }
  `,
})
export class App {
  private readonly principal = viewChild.required<ElementRef<HTMLElement>>('principal');

  constructor() {
    inject(SincronizacionService).iniciar();
    inject(Router)
      .events.pipe(
        filter((e) => e instanceof NavigationEnd),
        takeUntilDestroyed(),
      )
      .subscribe(() => this.principal().nativeElement.focus({ preventScroll: true }));
  }

  protected irAlContenido(evento: Event): void {
    evento.preventDefault();
    this.principal().nativeElement.focus();
  }
}
