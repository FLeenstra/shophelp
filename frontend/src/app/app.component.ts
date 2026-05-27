import { AfterViewInit, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import * as L from 'leaflet';

import { ApiService } from './api.service';
import { BasketItem, Product, RoutePlan, StoreTotal } from './api.models';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent implements OnInit, AfterViewInit {
  products: Product[] = [];
  quantities: Record<number, number> = {};

  storeTotals: StoreTotal[] | null = null;
  routePlan: RoutePlan | null = null;
  error: string | null = null;

  // Default start location: central Bolsward, Friesland.
  startLat = 53.0667;
  startLng = 5.5314;

  private map?: L.Map;
  private routeLayer?: L.LayerGroup;

  constructor(private api: ApiService) {}

  ngOnInit(): void {
    this.api.getProducts().subscribe({
      next: (products) => (this.products = products),
      error: () => (this.error = 'Could not load products. Is the backend running?')
    });
  }

  ngAfterViewInit(): void {
    this.map = L.map('map').setView([this.startLat, this.startLng], 12);
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution: '© OpenStreetMap contributors'
    }).addTo(this.map);
    this.routeLayer = L.layerGroup().addTo(this.map);
  }

  qty(productId: number): number {
    return this.quantities[productId] ?? 0;
  }

  changeQty(productId: number, delta: number): void {
    this.quantities[productId] = Math.max(0, this.qty(productId) + delta);
  }

  get basket(): BasketItem[] {
    return this.products
      .filter((p) => this.qty(p.id) > 0)
      .map((p) => ({ productId: p.id, quantity: this.qty(p.id) }));
  }

  compare(): void {
    this.error = null;
    this.api.compareBasket(this.basket).subscribe({
      next: (totals) => (this.storeTotals = totals),
      error: () => (this.error = 'Comparison failed.')
    });
  }

  planRoute(): void {
    this.error = null;
    this.api.planRoute(this.basket, this.startLat, this.startLng).subscribe({
      next: (plan) => {
        this.routePlan = plan;
        this.renderRoute(plan);
      },
      error: () => (this.error = 'Route planning failed.')
    });
  }

  private renderRoute(plan: RoutePlan): void {
    if (!this.map || !this.routeLayer) {
      return;
    }
    this.routeLayer.clearLayers();

    const startIcon = L.divIcon({ className: 'pin pin-start', html: '★', iconSize: [30, 30] });
    L.marker([this.startLat, this.startLng], { icon: startIcon })
      .bindTooltip('Start')
      .addTo(this.routeLayer);

    const points: L.LatLngTuple[] = [[this.startLat, this.startLng]];
    for (const stop of plan.stops) {
      points.push([stop.latitude, stop.longitude]);
      const icon = L.divIcon({ className: 'pin', html: String(stop.order), iconSize: [30, 30] });
      L.marker([stop.latitude, stop.longitude], { icon })
        .bindTooltip(`${stop.order}. ${stop.storeName}`)
        .addTo(this.routeLayer);
    }

    L.polyline(points, { color: '#2563eb', weight: 4, opacity: 0.7 }).addTo(this.routeLayer);

    if (points.length > 1) {
      this.map.fitBounds(L.latLngBounds(points).pad(0.2));
    }
  }
}
