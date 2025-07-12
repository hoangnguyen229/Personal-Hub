# Stage 1: Build Angular app
FROM node:16.20.2 AS build
WORKDIR /app
COPY personal-hub-frontend/package*.json ./
RUN npm install
COPY personal-hub-frontend/ ./
RUN npm run build -- --prod

# Stage 2: Serve with Nginx
FROM nginx:1.25-alpine
COPY nginx/nginx.conf /etc/nginx/nginx.conf
COPY nginx/conf.d/ /etc/nginx/conf.d/
COPY --from=build /app/dist/personal-hub-frontend /usr/share/nginx/html

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
