import { Component, OnInit, inject, signal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { ApiService, Order } from '../../services/api.services';
import { AuthHelperService } from '../../services/auth-helper.service';
import { FormsModule } from '@angular/forms';

const NEXT_STATUS: Record<string, string[]> = {
  CREADO: ['ACEPTADO', 'CANCELADO'],
  ACEPTADO: ['EN_PREPARACION', 'CANCELADO'],
  EN_PREPARACION: ['DESPACHADO', 'CANCELADO'],
  DESPACHADO: ['ENTREGADO'],
  ENTREGADO: [],
  CANCELADO: [],
};

@Component({
  selector: 'app-orders',
  imports: [FormsModule, DecimalPipe],
  template: `
    <div style="padding:1.5rem;">
      <h2>Pedidos</h2>

      @if (auth.hasRole('Cliente', 'Admin')) {
        <button (click)="createTestOrder()" style="margin-bottom:1rem;">
          + Crear pedido de prueba (producto id 1, cantidad 1)
        </button>
      }
      @if (error()) {
        <p style="color:red;">{{ error() }}</p>
      }

      @if (loading()) {
        <p>Cargando...</p>
      } @else {
        <table style="width:100%; border-collapse:collapse;">
          <thead>
            <tr style="text-align:left; border-bottom:1px solid #ccc;">
              <th>ID</th>
              <th>Cliente</th>
              <th>Estado</th>
              <th>Total</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            @for (o of orders(); track o.id) {
              <tr style="border-bottom:1px solid #eee;">
                <td>{{ o.id }}</td>
                <td>{{ o.customerName }}</td>
                <td>{{ o.status }}</td>
                <td>{{ o.total | number: '1.0-0' }}</td>
                <td>
                  @if (auth.hasRole('Admin', 'Operador')) {
                    @for (next of nextStatuses(o.status); track next) {
                      <button (click)="changeStatus(o, next)" style="margin-right:0.3rem;">
                        {{ next }}
                      </button>
                    }
                  }
                </td>
              </tr>
            }
          </tbody>
        </table>
      }
    </div>
  `,
})
export class OrdersComponent implements OnInit {
  private readonly api = inject(ApiService);
  auth = inject(AuthHelperService);

  orders = signal<Order[]>([]);
  loading = signal(true);
  error = signal('');

  ngOnInit() {
    this.load();
  }

  load() {
    this.loading.set(true);
    this.api.listOrders().subscribe({
      next: (data) => {
        this.orders.set(data);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  nextStatuses(current: string): string[] {
    return NEXT_STATUS[current] ?? [];
  }

  changeStatus(order: Order, status: string) {
    this.error.set('');
    this.api.updateOrderStatus(order.id, status).subscribe({
      next: () => this.load(),
      error: (e) => this.error.set(e.error?.detail ?? 'No se pudo cambiar el estado'),
    });
  }

  createTestOrder() {
    this.error.set('');
    this.api.createOrder([{ productId: 1, quantity: 1 }]).subscribe({
      next: () => this.load(),
      error: (e) => this.error.set(e.error?.detail ?? 'No se pudo crear el pedido'),
    });
  }
}
