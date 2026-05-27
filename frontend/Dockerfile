# --- Build stage: compile the Angular app (no local Node needed) ---
FROM node:22 AS build
WORKDIR /app
COPY package.json ./
RUN npm install
COPY . .
RUN npm run build

# --- Runtime stage: serve static files with nginx, proxy /api to the backend ---
FROM nginx:alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist/frontend/browser /usr/share/nginx/html
# Runtime config injection (Google Maps key from env) on container startup.
COPY docker-entrypoint.d/40-shophelp-config.sh /docker-entrypoint.d/40-shophelp-config.sh
RUN chmod +x /docker-entrypoint.d/40-shophelp-config.sh
EXPOSE 80
