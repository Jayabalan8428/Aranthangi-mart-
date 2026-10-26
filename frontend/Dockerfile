FROM node:22-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:1.27-alpine
# The official nginx image runs envsubst on /etc/nginx/templates/*.template at startup.
ENV BACKEND_URL=http://host.docker.internal:8080
COPY nginx.conf.template /etc/nginx/templates/default.conf.template
COPY --from=build /app/dist/aranthangi-mart/browser /usr/share/nginx/html
EXPOSE 80
