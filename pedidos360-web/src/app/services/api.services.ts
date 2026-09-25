import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../auth-config';

export interface Product {
  id: number;
  name: string;
  description: string;
  price: number;
  stock: number;
}

export interface OrderItem {
  productId: number;
  productName: string;
  quantity: number;
  unitPrice: number;
  subtotal: number;
}

export interface Order {
  id: number;
  customerName: string;
  customerEmail: string;
  status: string;
  createdAt: string;
  total: number;
  items: OrderItem[];
}

@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly http = inject(HttpClient);
  private readonly base = API_BASE_URL;

  // Catálogo
  listProducts(): Observable<Product[]> {
    return this.http.get<Product[]>(`${this.base}/api/catalog/products`);
  }

  createProduct(p: Omit<Product, 'id'>): Observable<Product> {
    return this.http.post<Product>(`${this.base}/api/catalog/products`, p);
  }

  updateProduct(id: number, p: Omit<Product, 'id'>): Observable<Product> {
    return this.http.put<Product>(`${this.base}/api/catalog/products/${id}`, p);
  }

  // Pedidos
  listOrders(): Observable<Order[]> {
    return this.http.get<Order[]>(`${this.base}/api/orders`);
  }

  createOrder(items: { productId: number; quantity: number }[]): Observable<Order> {
    return this.http.post<Order>(`${this.base}/api/orders`, { items });
  }

  updateOrderStatus(id: number, status: string): Observable<Order> {
    return this.http.put<Order>(`${this.base}/api/orders/${id}/status`, { status });
  }
}