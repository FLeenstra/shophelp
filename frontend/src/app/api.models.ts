// Mirrors the backend DTOs (see backend/.../dto). Kept in sync by hand for now;
// the live OpenAPI spec is served at /v3/api-docs and can drive generation later.

export interface Product {
  id: number;
  name: string;
  category: string;
  unit: string;
}

export interface BasketItem {
  productId: number;
  quantity: number;
}

export interface StoreTotal {
  storeId: number;
  storeName: string;
  total: number;
  itemsAvailable: number;
  itemsRequested: number;
  complete: boolean;
}

export interface RouteStop {
  order: number;
  storeId: number;
  storeName: string;
  latitude: number;
  longitude: number;
  subtotal: number;
  items: string[];
}

export interface RoutePlan {
  stops: RouteStop[];
  estimatedTotal: number;
  totalDistanceKm: number;
}

export interface Meta {
  pricesStubbed: boolean;
}
