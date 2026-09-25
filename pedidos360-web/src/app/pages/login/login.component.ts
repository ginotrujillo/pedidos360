import { Component, inject } from '@angular/core';
import { AuthHelperService } from '../../services/auth-helper.service';

@Component({
  selector: 'app-login',
  template: `
    <div style="display:flex; flex-direction:column; align-items:center; margin-top:4rem; gap:1rem;">
      <h1>Pedidos360</h1>
      <button (click)="auth.login()" style="padding:0.75rem 1.5rem;">
        Iniciar sesión con Microsoft
      </button>
    </div>
  `,
})
export class LoginComponent {
  auth = inject(AuthHelperService);
}