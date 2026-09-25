import { Component, OnInit, inject, signal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService, Product } from '../../services/api.services';
import { AuthHelperService } from '../../services/auth-helper.service';


@Component({
  selector: 'app-catalog',
  imports: [FormsModule,DecimalPipe],
  template: `
    <div style="padding:1.5rem; max-width:700px;">
      <h2>Catálogo</h2>

      @if (auth.hasRole('Admin')) {
        <fieldset style="margin-bottom:1.5rem; padding:1rem;">
          <legend>Nuevo producto</legend>
          <div style="display:flex; flex-direction:column; gap:0.5rem; max-width:350px;">
            <input [(ngModel)]="form.name" placeholder="Nombre" />
            <input [(ngModel)]="form.description" placeholder="Descripción" />
            <input [(ngModel)]="form.price" type="number" placeholder="Precio" />
            <input [(ngModel)]="form.stock" type="number" placeholder="Stock" />
            <button (click)="create()">Crear producto</button>
            @if (error()) { <p style="color:red;">{{ error() }}</p> }
          </div>
        </fieldset>
      }

      @if (loading()) {
        <p>Cargando...</p>
      } @else {
        <table style="width:100%; border-collapse:collapse;">
          <thead>
            <tr style="text-align:left; border-bottom:1px solid #ccc;">
              <th>Nombre</th><th>Precio</th><th>Stock</th>
            </tr>
          </thead>
          <tbody>
            @for (p of products(); track p.id) {
              <tr style="border-bottom:1px solid #eee;">
                <td>{{ p.name }}</td>
                <td>{{ p.price | number:'1.0-0' }}</td>
                <td>{{ p.stock }}</td>
              </tr>
            }
          </tbody>
        </table>
      }
    </div>
  `,
})
export class CatalogComponent implements OnInit {
  private readonly api = inject(ApiService);
  auth = inject(AuthHelperService);

  products = signal<Product[]>([]);
  loading = signal(true);
  error = signal('');
  form = { name: '', description: '', price: 0, stock: 0 };

  ngOnInit() {
    this.load();
  }

  load() {
    this.loading.set(true);
    this.api.listProducts().subscribe({
      next: (data) => { this.products.set(data); this.loading.set(false); },
      error: () => this.loading.set(false),
    });
  }

  create() {
    this.error.set('');
    this.api.createProduct(this.form).subscribe({
      next: () => { this.form = { name: '', description: '', price: 0, stock: 0 }; this.load(); },
      error: (e) => this.error.set(e.error?.detail ?? 'No se pudo crear el producto'),
    });
  }
}