import { AfterViewInit, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { ApiService } from './api.service';
import { ConfigService } from './config.service';
import { loadGoogleMaps } from './maps-loader';
import { BasketItem, Product, RoutePlan, StoreTotal } from './api.models';

declare const google: any;

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
  pricesStubbed = false;

  // Default start location: central Bolsward, Friesland.
  startLat = 53.0667;
  startLng = 5.5314;

  mapsAvailable = false;
  private map: any;
  private directionsRenderer: any;
  private fallbackLayers: any[] = [];

  constructor(private api: ApiService, private config: ConfigService) {
    this.mapsAvailable = !!this.config.googleMapsApiKey;
  }

  ngOnInit(): void {
    this.api.getProducts().subscribe({
      next: (products) => (this.products = products),
      error: () => (this.error = 'Could not load products. Is the backend running?')
    });
    this.api.getMeta().subscribe({
      next: (meta) => (this.pricesStubbed = meta.pricesStubbed),
      error: () => {}
    });
  }

  ngAfterViewInit(): void {
    // Warm up the Maps script so the map is ready by the time a route is planned.
    if (this.mapsAvailable) {
      loadGoogleMaps(this.config.googleMapsApiKey).catch(() => (this.mapsAvailable = false));
    }
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
        this.renderMap(plan);
      },
      error: () => (this.error = 'Route planning failed.')
    });
  }

  /** Deep link that opens Google Maps with driving directions through every stop (no API key needed). */
  get googleMapsUrl(): string {
    if (!this.routePlan || this.routePlan.stops.length === 0) {
      return '';
    }
    const stops = this.routePlan.stops;
    const origin = `${this.startLat},${this.startLng}`;
    const last = stops[stops.length - 1];
    const destination = `${last.latitude},${last.longitude}`;
    const waypoints = stops
      .slice(0, -1)
      .map((s) => `${s.latitude},${s.longitude}`)
      .join('|');
    let url = `https://www.google.com/maps/dir/?api=1&origin=${origin}&destination=${destination}&travelmode=driving`;
    if (waypoints) {
      url += `&waypoints=${encodeURIComponent(waypoints)}`;
    }
    return url;
  }

  private async renderMap(plan: RoutePlan): Promise<void> {
    if (!this.mapsAvailable || plan.stops.length === 0) {
      return;
    }
    try {
      await loadGoogleMaps(this.config.googleMapsApiKey);
    } catch {
      this.mapsAvailable = false;
      return;
    }
    const el = document.getElementById('map');
    if (!el) {
      return;
    }
    if (!this.map) {
      this.map = new google.maps.Map(el, {
        center: { lat: this.startLat, lng: this.startLng },
        zoom: 12,
        mapTypeControl: false,
        streetViewControl: false
      });
    }

    const stops = plan.stops;
    const last = stops[stops.length - 1];
    const request = {
      origin: { lat: this.startLat, lng: this.startLng },
      destination: { lat: last.latitude, lng: last.longitude },
      waypoints: stops.slice(0, -1).map((s) => ({
        location: { lat: s.latitude, lng: s.longitude },
        stopover: true
      })),
      travelMode: google.maps.TravelMode.DRIVING
    };

    if (!this.directionsRenderer) {
      this.directionsRenderer = new google.maps.DirectionsRenderer({ map: this.map });
    }

    const service = new google.maps.DirectionsService();
    service.route(request, (result: any, status: any) => {
      if (status === 'OK') {
        this.clearFallback();
        this.directionsRenderer.setDirections(result);
      } else {
        // No road route available — fall back to markers + straight lines.
        this.directionsRenderer.set('directions', null);
        this.drawFallback(plan);
      }
    });
  }

  private drawFallback(plan: RoutePlan): void {
    this.clearFallback();
    const path = [
      { lat: this.startLat, lng: this.startLng },
      ...plan.stops.map((s) => ({ lat: s.latitude, lng: s.longitude }))
    ];
    const bounds = new google.maps.LatLngBounds();
    path.forEach((point, i) => {
      const marker = new google.maps.Marker({
        position: point,
        map: this.map,
        label: i === 0 ? 'S' : String(i)
      });
      this.fallbackLayers.push(marker);
      bounds.extend(point);
    });
    const line = new google.maps.Polyline({
      path,
      map: this.map,
      strokeColor: '#2563eb',
      strokeWeight: 4,
      strokeOpacity: 0.7
    });
    this.fallbackLayers.push(line);
    this.map.fitBounds(bounds);
  }

  private clearFallback(): void {
    this.fallbackLayers.forEach((layer) => layer.setMap(null));
    this.fallbackLayers = [];
  }
}
