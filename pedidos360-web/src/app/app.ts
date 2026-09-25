import { Component, OnInit, inject } from '@angular/core';
import { RouterLink, RouterOutlet } from '@angular/router';
import { MsalService } from '@azure/msal-angular';
import { AuthHelperService } from './services/auth-helper.service';

@Component({
  selector: 'app-root',
  imports: [RouterLink, RouterOutlet],
  template: `
    @if (auth.isLoggedIn) {
      <nav style="display:flex; gap:1rem; padding:1rem; background:#222; color:white; align-items:center;">
        <strong>Pedidos360</strong>
        <a routerLink="/dashboard" style="color:white;">Dashboard</a>
        <a routerLink="/orders" style="color:white;">Pedidos</a>
        <a routerLink="/catalog" style="color:white;">Catálogo</a>
        <span style="margin-left:auto;">{{ auth.userName() }} ({{ auth.roles().join(', ') }})</span>
        <button (click)="auth.logout()">Salir</button>
      </nav>
    }
    <router-outlet />
  `,
})
export class App implements OnInit {
  private readonly msal = inject(MsalService);
  auth = inject(AuthHelperService);

  ngOnInit() {
    this.msal.handleRedirectObservable().subscribe({
      next: () => this.auth.loadFromActiveAccount(),
      error: (e) => console.error(e),
    });
  }
}