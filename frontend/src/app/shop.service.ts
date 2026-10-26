import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { OrderRequest, OrderResponse, Product, SortOption, StoreInfo } from './product.model';

@Injectable({ providedIn: 'root' })
export class ShopService {
  private readonly http = inject(HttpClient);

  getProducts(search = '', category = 'All', sort: SortOption = 'default'): Observable<Product[]> {
    const params: Record<string, string> = { sort };
    if (search.trim()) params['search'] = search.trim();
    if (category !== 'All') params['category'] = category;
    return this.http.get<Product[]>('/api/products', { params });
  }

  getCategories(): Observable<string[]> {
    return this.http.get<string[]>('/api/products/categories');
  }

  getStoreInfo(): Observable<StoreInfo> {
    return this.http.get<StoreInfo>('/api/store');
  }

  placeOrder(payload: OrderRequest): Observable<OrderResponse> {
    return this.http.post<OrderResponse>('/api/orders', payload);
  }

  getOrders(phone: string): Observable<OrderResponse[]> {
    return this.http.get<OrderResponse[]>('/api/orders', { params: { phone } });
  }
}
