import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { BasketItem, Meta, Product, RoutePlan, StoreTotal } from './api.models';

// Relative path: in production nginx proxies /api to the backend (same origin).
const API = '/api';

@Injectable({ providedIn: 'root' })
export class ApiService {
  constructor(private http: HttpClient) {}

  getProducts(): Observable<Product[]> {
    return this.http.get<Product[]>(`${API}/products`);
  }

  getMeta(): Observable<Meta> {
    return this.http.get<Meta>(`${API}/meta`);
  }

  compareBasket(items: BasketItem[]): Observable<StoreTotal[]> {
    return this.http.post<StoreTotal[]>(`${API}/basket/compare`, { items });
  }

  planRoute(items: BasketItem[], startLat: number, startLng: number): Observable<RoutePlan> {
    return this.http.post<RoutePlan>(`${API}/route/plan`, { items, startLat, startLng });
  }
}
