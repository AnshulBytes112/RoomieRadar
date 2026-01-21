# 🏠 RoomieRadar Backend (In Progress)

**RoomieRadar** is an upcoming backend service built with **Java Spring Boot** to help students find compatible roommates, discover available rooms, and manage rental agreements.  
It uses **JWT authentication** for secure access, **role-based authorization**, and **Apache Kafka** for real-time, event-driven communication.

---

## 📌 Project Status
🚧 **Ongoing Development** – Features are being implemented in phases.

---


This project uses JWT for secure authentication, role-based authorization, and is designed to manage user profiles, room listings, bookings, and favorites.

## ⚙️ Tech Stack

- **Backend:** Java 17, Spring Boot 3
- **Security:** Spring Security, JSON Web Tokens (JWT)
- **Database:** PostgreSQL
- **Data Access:** Spring Data JPA
- **Build Tool:** Maven
- **Libraries:** Lombok, ModelMapper

## ✨ Key Features

- **Authentication:** Secure user registration and login using JWT.
- **User Profile Management:** Create, view, and update user profiles.
- **Room Listings:**
    - Create, read, update, and delete room listings.
    - Owner-only authorization for deleting/updating rooms.
- **Dynamic Search:** Search for rooms with filters for location, budget, room type, number of bedrooms, and bathrooms.
- **Data Modeling:** Comprehensive entity structure including Users, Rooms, Bookings, Favourites, and Roommate Profiles.

## 🚀 Getting Started

### Prerequisites

- Java 17 or later
- Maven 3.x
- PostgreSQL database

### Installation

1.  **Clone the repository:**
    ```sh
    git clone https://github.com/anshulbytes112/RoomieRadar.git
    cd RoomieRadar
    ```

2.  **Configure the database:**
    - Create a PostgreSQL database (e.g., `roomie_radar`).
    - Update the `src/main/resources/application.properties` file with your database credentials:
      ```properties
      spring.datasource.url=jdbc:postgresql://localhost:5432/roomie_radar
      spring.datasource.username=<YOUR_POSTGRES_USERNAME>
      spring.datasource.password=<YOUR_POSTGRES_PASSWORD>
      spring.datasource.driver-class-name=org.postgresql.Driver
      
      # JPA/Hibernate settings
      spring.jpa.hibernate.ddl-auto=update
      spring.jpa.show-sql=true
      spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
      
      # JWT Secret Key
      jwt.secret=TaK+HaV^uvCHEFsE94an30sh9^k*Z8$V
      ```

3.  **Build and run the application:**
    ```sh
    mvn spring-boot:run
    ```
    The application will start on `http://localhost:8080`.

## <caption>API Endpoints

All endpoints are prefixed with `/api`.

### Authentication (`/auth`)

| Method | Endpoint          | Description                                |
| :----- | :---------------- | :----------------------------------------- |
| `POST` | `/login`          | Authenticates a user and returns a JWT.    |
| `POST` | `/register`       | Registers a new user.                      |
| `GET`  | `/me`             | Retrieves the current authenticated user.  |
| `PUT`  | `/profile`        | Updates the profile of the current user.   |

### Rooms (`/rooms`)

| Method | Endpoint          | Description                                                                    |
| :----- | :---------------- | :----------------------------------------------------------------------------- |
| `GET`  | `/`               | Retrieves all room listings.                                                   |
| `POST` | `/`               | Creates a new room listing (authentication required).                          |
| `GET`  | `/{id}`           | Retrieves a specific room by its ID.                                           |
| `DELETE`| `/{id}`          | Deletes a room by its ID (authentication and ownership required).                |
| `GET`  | `/search`         | Searches for rooms with query parameters (`location`, `budget`, `roomType`, etc.). |

#### Search Query Parameters

Example: `/api/rooms/search?location=downtown&budget=500-1000&bedrooms=2`

-   `location` (String)
-   `budget` (String, e.g., "500-1000")
-   `roomType` (String, e.g., "Private", "Shared")
-   `bedrooms` (Integer)
-   `bathrooms` (Integer)

## 📁 Project Structure

```
.
└── src/main/java/com/anshul/RoomieRadarBackend/
    ├── Controller      # REST API controllers (e.g., AuthController, RoomController)
    ├── Service         # Business logic (e.g., UserService, RoomService)
    ├── repository      # Data access layer (JPA repositories)
    ├── entity          # JPA entity classes (e.g., User, Room)
    ├── dto             # Data Transfer Objects for API requests/responses
    ├── config          # Spring Security configuration
    ├── filter          # JWT request filter
    └── utils           # Utility classes (e.g., JwtUtils)
