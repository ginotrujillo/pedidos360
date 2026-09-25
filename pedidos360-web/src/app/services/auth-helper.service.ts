import { Injectable, inject, signal } from '@angular/core';
import { MsalService } from '@azure/msal-angular';
import { apiScopes } from '../auth-config';

@Injectable({ providedIn: 'root' })
export class AuthHelperService {
  private readonly auth = inject(MsalService);

  userName = signal('');
  userEmail = signal('');
  roles = signal<string[]>([]);

  get isLoggedIn(): boolean {
    return this.auth.instance.getAllAccounts().length > 0;
  }

  loadFromActiveAccount() {
    const accounts = this.auth.instance.getAllAccounts();
    if (!accounts.length) return;
    const account = accounts[0];
    this.auth.instance.setActiveAccount(account);
    this.userName.set(account.name ?? account.username);
    this.userEmail.set(account.username);

    this.auth.acquireTokenSilent({ scopes: apiScopes }).subscribe((result) => {
      const payload = JSON.parse(
        atob(result.accessToken.split('.')[1].replace(/-/g, '+').replace(/_/g, '/'))
      );
      this.roles.set(payload.roles ?? []);
    });
  }

  hasRole(...allowed: string[]): boolean {
    return this.roles().some((r) => allowed.includes(r));
  }

  login() {
    this.auth.loginRedirect({ scopes: apiScopes });
  }

  logout() {
    this.auth.logoutRedirect();
  }
}