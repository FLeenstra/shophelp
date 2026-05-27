#!/bin/sh
# Runs automatically on nginx container startup (scripts in /docker-entrypoint.d/
# are executed by the base image's entrypoint). Writes runtime config that the
# Angular app reads via window.__SHOPHELP_CONFIG__, so the Google Maps key can be
# supplied as an env var without rebuilding the image.
set -e

cat > /usr/share/nginx/html/config.js <<EOF
window.__SHOPHELP_CONFIG__ = { googleMapsApiKey: "${GOOGLE_MAPS_API_KEY:-}" };
EOF

if [ -n "${GOOGLE_MAPS_API_KEY:-}" ]; then
  echo "shophelp: config.js written (Google Maps key set)"
else
  echo "shophelp: config.js written (no Google Maps key; embedded map disabled)"
fi
