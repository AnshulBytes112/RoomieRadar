# 🏠 RoomieRadar Backend

**RoomieRadar** is a production‑ready Spring Boot backend for a roommate‑and‑room‑matching platform.  
It enables students and young professionals to create roommate profiles, list/search rooms, manage bookings, and chat in real time after mutual consent.

---

## ✨ Key Features

- **Secure Authentication:** JWT‑based auth with role‑aware security.
- **User & Roommate Profiles:** One‑to‑one roommate profile per user with lifestyle/budget/interests.
- **Room Listings:** Full CRUD with owner‑only edit/delete; rich metadata (photos, tags, amenities, pricing).
- **Dynamic Search:** Filter rooms by location, budget range, room type, bedrooms, bathrooms.
- **Favorites & Bookings:** Bookmark rooms and submit booking/visit requests.
- **Gated Real‑time Chat:**  
  - Users send a **message request**; only after acceptance can they chat.  
  - WebSocket (STOMP) with JWT‑authenticated handshake for instant messaging and notifications.
- **Clean Architecture:** Controllers → Services → Repositories; DTOs + Mappers for API boundaries.

---

## ⚙️ Tech Stack

- **Backend:** Java 17, Spring Boot 3
- **Security:** Spring Security, JWT (stateless), BCrypt password encoding
- **Database:** PostgreSQL with Spring Data JPA/Hibernate
- **Real‑time:** WebSocket with STOMP messaging (`SimpMessagingTemplate`)
- **Build:** Maven
- **Libraries:** Lombok, Jackson, Bean Validation (ready to enable)

---

## 🚀 Getting Started

### Prerequisites

- Java 17+
- Maven 3.x
- PostgreSQL 13+

### Installation

1. **Clone**
   ```sh
   git clone https://github.com/anshulbytes112/RoomieRadar.git
   cd RoomieRadar/RoomieRadarBackend
   ```

2. **Database**
   ```sql
   CREATE DATABASE roomie_radar;
   ```
   Update `src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/roomie_radar
   spring.datasource.username=<YOUR_POSTGRES_USERNAME>
   spring.datasource.password=<YOUR_POSTGRES_PASSWORD>
   spring.datasource.driver-class-name=org.postgresql.Driver

   spring.jpa.hibernate.ddl-auto=update
   spring.jpa.show-sql=false
   spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect

   jwt.secret=TaK+HaV^uvCHEFsE94an30sh9^k*Z8$V
   ```

3. **Run**
   ```sh
   mvn spring-boot:run
   ```
   Service starts on `http://localhost:8080`.

---

## 📡 API Overview

All REST endpoints are prefixed with `/api`. WebSocket endpoint: `/ws`.

### Authentication (`/api/auth`)

| Method | Endpoint     | Description                              |
|--------|--------------|------------------------------------------|
| POST   | `/login`     | Authenticate → JWT token + user info       |
| POST   | `/register`  | Register new user (email/username unique)   |
| GET    | `/me`        | Current authenticated user                 |
| PUT    | `/profile`   | Update current user profile                |

### Rooms (`/api/rooms`)

| Method | Endpoint                     | Description                                           |
|--------|------------------------------|-------------------------------------------------------|
| GET    | `/`                          | List all rooms (paginated)               |
| POST   | `/`                          | Create room (auth required)                             |
| GET    | `/{id}`                      | Room details by ID                                    |
| PUT    | `/{id}`                      | Update room (owner only)                               |
| DELETE | `/{id}`                      | Delete room (owner only)                               |
| GET    | `/search`                     | Search rooms (location, budget range, type, bedrooms, bathrooms) |
| GET    | `/my-listings`               | Current user’s rooms                                   |

#### Search Example
```bash
GET /api/rooms/search?location=downtown&budget=500-1000&roomType=Private&bedrooms=2
```

### Roommate Profiles (`/api/roommates`)

| Method | Endpoint      | Description                              |
|--------|---------------|------------------------------------------|
| POST   | `/`            | Create roommate profile (one per user)       |
| GET    | `/`            | List all roommate profiles                  |
| GET    | `/search`       | Search profiles (age, lifestyle, budget, location, occupation, gender) |
| PUT    | `/{id}`        | Update own roommate profile                |

### Favorites (`/api/favorites`)

| Method | Endpoint                | Description                                 |
|--------|-------------------------|---------------------------------------------|
| GET    | `/`                     | Current user’s favorites                         |
| POST   | `/`                     | Add favorite (roomId in body)                    |
| DELETE | `/{roomId}`             | Remove favorite                                |
| GET    | `/{roomId}/check`        | Is room favorited by current user?               |

### Bookings (`/api/bookings`)

| Method | Endpoint | Description                         |
|--------|----------|-------------------------------------|
| POST   | `/`       | Create booking request for a room       |

### Chat & Message Requests

| Method | Endpoint                              | Description                                               |
|--------|---------------------------------------|-----------------------------------------------------------|
| POST   | `/api/message-requests`                 | Send a message request to start a chat                      |
| GET    | `/api/message-requests/inbox`          | Incoming requests for current user                            |
| GET    | `/api/message-requests/sent`           | Sent requests by current user                               |
| POST   | `/api/message-requests/{id}/respond`   | Accept/reject a request (creates Conversation if accepted)   |
| GET    | `/api/conversations`                   | List conversations for current user                         |
| GET    | `/api/conversations/{id}/messages`      | Messages in a conversation (participants only)               |
| POST   | `/api/conversations/{id}/messages`      | Send a message in a conversation (participants only)         |

#### WebSocket (STOMP)

- Connect to `ws://localhost:8080/ws?token=<jwt>`
- Send messages to `/app/private-message`
- Subscribe to `/user/queue/messages` (private messages) and `/topic/requests/{userId}` (request status updates)

---

## 🏗️ Project Structure

```bash
src/main/java/com/anshul/RoomieRadarBackend/
├── Controller/          # REST endpoints
├── Service/             # Business logic
├── repository/          # Spring Data JPA
├── entity/              # JPA entities (User, Room, Booking, Favourite, Image, RoomateProfile)
├── Model/               # Chat entities (Conversation, Message, MessageRequest)
├── dto/                 # Data Transfer Objects
├── Mapper/              # Entity ↔ DTO mappers
├── config/              # Security & WebSocket config
├── filter/              # JWT filter
└── utils/               # JWT utilities
```

---

## 🔧 Production Considerations

- **Pagination:** Already present on list endpoints (e.g., `Pageable` in rooms and roommates); extend to other list endpoints as needed.
- **Validation:** Enable Bean Validation on DTOs (`@Valid`) and global exception handling.
- **Security:** Remove password logging; add refresh tokens, rate limiting, and account lockout.
- **Performance:** Optimize N+1 queries (e.g., conversations + last message) and add DB indexes.
- **Deployment:** Stateless JWT enables horizontal scaling behind a load balancer. Use an external WebSocket broker (Redis/RabbitMQ) for scaling chat.
- **Monitoring:** Add Actuator health checks and metrics.

---

## 📄 License

MIT License – see `LICENSE` file.
