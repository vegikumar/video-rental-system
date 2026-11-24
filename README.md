# RentVideo — Video Rental System

RESTful API for an online video rental service built with **Spring Boot 3**, **Spring Security (Basic Auth)**, **Spring Data JPA**, and **MySQL**.

## Features

- User registration & login with **BCrypt** password hashing
- Role-based access: `CUSTOMER` (default) and `ADMIN`
- **Basic Authentication** for protected endpoints
- Video catalogue management (CRUD for ADMIN)
- Browse available videos (authenticated users)
- Rent & return videos (max **2** active rentals per user)
- Global exception handling with meaningful HTTP status codes

## Tech Stack

| Layer | Technology |
|-------|------------|
| Language | Java 17+ |
| Framework | Spring Boot 3.4 |
| Security | Spring Security (HTTP Basic) |
| Persistence | Spring Data JPA + Hibernate |
| Database | MySQL 8 (H2 for tests) |
| Build | Maven |

## Project Structure

```
src/main/java/com/crio/rentvideo
├── config          # Security configuration
├── controller      # REST controllers
├── dto             # Request/response DTOs
├── entity          # JPA entities
├── exception       # Custom exceptions + global handler
├── repository      # Spring Data repositories
├── security        # UserDetails implementation
└── service         # Business logic
```

## Prerequisites

- JDK 17+ (JDK 17 recommended; JDK 26 may need a newer Lombok)
- Maven 3.9+ (or use the included Maven Wrapper)
- MySQL 8 running locally

## Database Setup

```sql
CREATE DATABASE rentvideo;
```

Update credentials in `src/main/resources/application.properties`:

```properties
spring.datasource.username=root
spring.datasource.password=your_password
```

> Tip: To run without MySQL, uncomment the H2 section in `application.properties`.

## Run the Application

```bash
./mvnw spring-boot:run
```

Windows:

```bash
mvnw.cmd spring-boot:run
```

API base URL: `http://localhost:8080`

## Run Tests

```bash
./mvnw test
```

## API Endpoints

### Public (no auth)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/auth/register` | Register a new user |
| `POST` | `/api/auth/login` | Validate credentials (Basic Auth used for subsequent calls) |

### Videos (authenticated)

| Method | Endpoint | Roles | Description |
|--------|----------|-------|-------------|
| `GET` | `/api/videos` | CUSTOMER, ADMIN | List available videos (`?availableOnly=false` for all) |
| `GET` | `/api/videos/{id}` | CUSTOMER, ADMIN | Get video by id |
| `POST` | `/api/videos` | ADMIN | Create video |
| `PUT` | `/api/videos/{id}` | ADMIN | Update video |
| `DELETE` | `/api/videos/{id}` | ADMIN | Delete video |

### Rentals (authenticated)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/rentals/users/{userId}/videos/{videoId}` | Rent a video |
| `PUT` | `/api/rentals/{rentalId}/return` | Return a rented video |
| `GET` | `/api/rentals/users/{userId}/active` | List active rentals |

## Sample Requests

### Register (CUSTOMER by default)

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"customer@example.com\",\"password\":\"password123\",\"firstName\":\"Jane\",\"lastName\":\"Doe\"}"
```

### Register ADMIN

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"admin@example.com\",\"password\":\"admin123\",\"firstName\":\"Admin\",\"lastName\":\"User\",\"role\":\"ADMIN\"}"
```

### Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"customer@example.com\",\"password\":\"password123\"}"
```

### Create Video (ADMIN — Basic Auth)

```bash
curl -u admin@example.com:admin123 \
  -X POST http://localhost:8080/api/videos \
  -H "Content-Type: application/json" \
  -d "{\"title\":\"Inception\",\"director\":\"Christopher Nolan\",\"genre\":\"Sci-Fi\"}"
```

### Browse Available Videos

```bash
curl -u customer@example.com:password123 http://localhost:8080/api/videos
```

### Rent a Video

```bash
curl -u customer@example.com:password123 \
  -X POST http://localhost:8080/api/rentals/users/1/videos/1
```

### Return a Video

```bash
curl -u customer@example.com:password123 \
  -X PUT http://localhost:8080/api/rentals/1/return
```

## Authentication Notes

- Passwords are stored using **BCrypt**.
- Protected endpoints expect HTTP Basic Auth: `Authorization: Basic base64(email:password)`.
- Role defaults to `CUSTOMER` when omitted during registration.
- Only `ADMIN` can create, update, or delete videos.

## Error Handling

| Status | When |
|--------|------|
| `400` | Validation errors, rental limit exceeded, video unavailable |
| `401` | Missing/invalid credentials |
| `403` | Authenticated but wrong role |
| `404` | Entity not found |

## Postman

Import `postman/RentVideo.postman_collection.json` for a ready-made collection with sample requests.
