// Loads the Google Maps JavaScript API on demand, exactly once.
let loadPromise: Promise<void> | null = null;

export function loadGoogleMaps(apiKey: string): Promise<void> {
  if ((window as any).google?.maps) {
    return Promise.resolve();
  }
  if (!apiKey) {
    return Promise.reject(new Error('No Google Maps API key configured'));
  }
  if (loadPromise) {
    return loadPromise;
  }
  loadPromise = new Promise<void>((resolve, reject) => {
    const script = document.createElement('script');
    script.src = `https://maps.googleapis.com/maps/api/js?key=${encodeURIComponent(apiKey)}`;
    script.async = true;
    script.defer = true;
    script.onload = () => resolve();
    script.onerror = () => {
      loadPromise = null;
      reject(new Error('Failed to load the Google Maps JS API'));
    };
    document.head.appendChild(script);
  });
  return loadPromise;
}
