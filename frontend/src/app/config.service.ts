import { Injectable } from '@angular/core';

// Runtime config injected by the nginx container (config.js) from env vars.
// See frontend/docker-entrypoint.d/40-shophelp-config.sh.
declare global {
  interface Window {
    __SHOPHELP_CONFIG__?: { googleMapsApiKey?: string };
  }
}

@Injectable({ providedIn: 'root' })
export class ConfigService {
  get googleMapsApiKey(): string {
    return window.__SHOPHELP_CONFIG__?.googleMapsApiKey ?? '';
  }
}
