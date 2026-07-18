# --- Build stage: produce the static SPA bundle ---
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
# Thumbnails are served same-origin via nginx (see nginx.conf /storage).
ARG VITE_MEDIA_BASE_URL=/storage
ENV VITE_MEDIA_BASE_URL=$VITE_MEDIA_BASE_URL
RUN npm run build

# --- Runtime stage: nginx serving the bundle + proxying /api and /storage ---
FROM nginx:alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
