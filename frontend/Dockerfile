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
EXPOSE 80
