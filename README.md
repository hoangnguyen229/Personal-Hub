# Personal Hub

## 📋 Overview
Personal Hub is a comprehensive web application that serves as a centralized platform for managing personal information, blogging, messaging, and more. The project combines a modern Angular frontend with a robust Spring Boot backend, offering a feature-rich personal management system.

## 🌟 Features

### Authentication & Authorization
- Multi-factor authentication with OTP verification
- OAuth2 social login integration (Google, GitHub)
- JWT-based authentication
- Role-based access control

### Content Management
- Blog creation and publishing system
- Rich text editor for content creation
- File and image upload with Cloudinary integration
- Category and tag management
- Content search with Elasticsearch (Vietnamese language support)

### Communication
- Real-time messaging using WebSocket/STOMP
- Email notifications with templating
- User notifications system

### User Management
- User profiles
- Password reset functionality
- Account settings and preferences
- User status

## 🛠️ Technology Stack

### Frontend
- **Framework**: Angular 10
- **UI Components**: Bootstrap, Fontawesome, Syncfusion
- **State Management**: NgRx
- **Real-time Communication**: STOMP.js

### Backend
- **Framework**: Spring Boot 3.4.4
- **Security**: Spring Security, JWT
- **Database**: Oracle
- **ORM**: Hibernate/JPA
- **API Documentation**: Springdoc OpenAPI
- **Search Engine**: Elasticsearch with Vietnamese language analysis

### DevOps & Infrastructure
- **Containerization**: Docker, Docker Compose
- **Message Broker**: RabbitMQ
- **Caching**: Redis
- **Monitoring**: Redis Insight

## 📊 Project Structure

### Backend Structure (personal-hub-backend)
- `src/main/java/hoangnguyen/` - Main Java source code
- `src/main/resources/` - Application properties, templates and static resources
- `src/test/` - Test files
- `elasticsearch-analysis-vietnamese/` - Vietnamese language analysis plugin for Elasticsearch
- `target/` - Compiled output

### Frontend Structure (personal-hub-frontend)
- `src/app/` - Angular application components and modules
  - `auth/` - Authentication components (login, register, forgot password)
  - `components/` - Shared UI components
  - `core/` - Core services, models, and guards
  - `layouts/` - Page layouts and routing
- `src/assets/` - Static assets like images and icons
- `src/environments/` - Environment configuration
- `e2e/` - End-to-end testing


## 🚀 Getting Started

### Prerequisites
- Java 17
- Node.js & npm
- Docker & Docker Compose
- Oracle Database instance

### Environment Setup
Create a `.env` file in the root directory with the same variables as the `.env.sample` file

### Running with Docker
```bash
docker-compose up -d
```
