import { Component, OnInit, inject, signal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { ApiService, Order, Product } from '../../services/api.services';
import { AuthHelperService } from '../../services/auth-helper.service';

@Component({
  selector: 'app-dashboard',
  imports: [DecimalPipe],
  template: `
    <div style="padding:1.5rem;">
      <h2>Dashboard</h2>
      <p>Bienvenido, {{ auth.userName() }} — Rol: {{ auth.roles().join(', ') || '(sin rol)' }}</p>

      <div style="display:flex; gap:1.5rem; margin-top:1rem;">
        <div style="border:1px solid #ccc; padding:1rem; border-radius:8px;">
          <h3>Pedidos</h3>
          <p style="font-size:2rem; margin:0;">{{ orders().length }}</p>
          <p>{{ auth.hasRole('Cliente') ? 'Tus pedidos' : 'Total en el sistema' }}</p>
        </div>

        @if (auth.hasRole('Admin', 'Operador')) {
          <div style="border:1px solid #ccc; padding:1rem; border-radius:8px;">
            <h3>Pendientes de gestionar</h3>
            <p style="font-size:2rem; margin:0;">{{ pending() }}</p>
            <p>CREADO o ACEPTADO</p>
          </div>
        }

        @if (auth.hasRole('Admin')) {
          <div style="border:1px solid #ccc; padding:1rem; border-radius:8px;">
            <h3>Productos con bajo stock</h3>
            <p style="font-size:2rem; margin:0;">{{ lowStock() }}</p>
            <p>Stock menor a 5</p>
          </div>
        }
      </div>
    </div>
  `,
})
export class DashboardComponent implements OnInit {
  private readonly api = inject(ApiService);
  auth = inject(AuthHelperService);

  orders = signal<Order[]>([]);
  products = signal<Product[]>([]);

  ngOnInit() {
    this.api.listOrders().subscribe((data) => this.orders.set(data));
    if (this.auth.hasRole('Admin')) {
      this.api.listProducts().subscribe((data) => this.products.set(data));
    }
  }

  pending(): number {
    return this.orders().filter((o) => o.status === 'CREADO' || o.status === 'ACEPTADO').length;
  }

  lowStock(): number {
    return this.products().filter((p) => p.stock < 5).length;
  }
}