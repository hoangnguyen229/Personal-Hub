# Stage 1: Build Angular application
FROM node:16.20.2 AS build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build -- --prod

# Stage 2: Serve with Nginx
FROM nginx:alpine
COPY --from=build /app/dist/personal-hub-frontend /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]