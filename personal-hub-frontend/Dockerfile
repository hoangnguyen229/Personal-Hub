# Stage 1: Build Angular app
FROM node:16.20.2 AS build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build -- --prod

# Stage 2: Just store the build artifacts in image
FROM alpine:latest
WORKDIR /dist
COPY --from=build /app/dist/personal-hub-frontend ./