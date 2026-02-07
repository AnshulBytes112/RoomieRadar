# RoomieRadar Backend - Deployment Guide

This guide covers how to configure and deploy the RoomieRadar backend application using Spring Boot profiles.

## Table of Contents
- [Spring Profiles Overview](#spring-profiles-overview)
- [Environment Variables](#environment-variables)
- [Running Locally](#running-locally)
- [Building for Production](#building-for-production)
- [Health Checks](#health-checks)
- [Troubleshooting](#troubleshooting)

## Spring Profiles Overview

The application supports two profiles:

### Development Profile (`dev`)
- **Purpose**: Local development
- **Database**: Local PostgreSQL at `localhost:5432`
- **Logging**: Debug mode enabled
- **CORS**: Allows `localhost:3000`, `localhost:5173`, `localhost:5174`
- **File Uploads**: Stored in `./uploads` directory
- **Credentials**: Hardcoded for convenience (NOT for production)

### Production Profile (`prod`)
- **Purpose**: Production deployment
- **Database**: Configured via environment variables
- **Logging**: Minimal logging (WARN level)
- **CORS**: Restricted to specified domain
- **File Uploads**: Configured via environment variable
- **Credentials**: All sensitive data from environment variables
- **Connection Pooling**: Optimized with HikariCP

## Environment Variables

### Required for Production

When running in production mode (`prod` profile), the following environment variables **MUST** be set:

| Variable | Description | Example |
|----------|-------------|---------|
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | `prod` |
| `DB_URL` | PostgreSQL database URL | `jdbc:postgresql://db.example.com:5432/roomieradar` |
| `DB_USERNAME` | Database username | `roomieradar_user` |
| `DB_PASSWORD` | Database password | `secure_password_123` |
| `JWT_SECRET` | JWT signing secret (min 32 chars) | `your_secure_jwt_secret_key_here` |
| `MAIL_USERNAME` | Email username for notifications | `your-email@gmail.com` |
| `MAIL_PASSWORD` | Email password/app password | `your_app_password` |

### Optional Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `PORT` | Server port | `8080` |
| `ALLOWED_ORIGINS` | CORS allowed origins (comma-separated) | `https://yourdomain.com` |
| `FILE_UPLOAD_DIR` | Directory for file uploads | `/app/uploads` |
| `MAIL_HOST` | SMTP server host | `smtp.gmail.com` |
| `MAIL_PORT` | SMTP server port | `587` |

## Running Locally

### Development Mode (Default)

```powershell
# Navigate to backend directory
cd c:\Users\ANSHUL\RoomieRadar\RoomieRadarBackend

# Run with Maven
mvn spring-boot:run
```

This will automatically use the `dev` profile (default).

### Explicitly Set Development Profile

```powershell
# Using Maven
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Or set environment variable
$env:SPRING_PROFILES_ACTIVE="dev"
mvn spring-boot:run
```

### Testing Production Configuration Locally

```powershell
# Set required environment variables
$env:SPRING_PROFILES_ACTIVE="prod"
$env:DB_URL="jdbc:postgresql://localhost:5432/roomie_radar"
$env:DB_USERNAME="postgres"
$env:DB_PASSWORD="12345"
$env:JWT_SECRET="TaK+HaV^uvCHEFsE94an30sh9^k*Z8$V"
$env:MAIL_USERNAME="roomieradar2@gmail.com"
$env:MAIL_PASSWORD="noqz izmu fvvw cehv"
$env:ALLOWED_ORIGINS="http://localhost:5173,http://localhost:3000"

# Run application
mvn spring-boot:run
```

## Building for Production

### 1. Build JAR File

```powershell
# Clean and build
mvn clean package -DskipTests

# JAR file will be in: target/RoomieRadarBackend-0.0.1-SNAPSHOT.jar
```

### 2. Run JAR with Production Profile

```powershell
# Set environment variables first (see above section)

# Run the JAR
java -jar target/RoomieRadarBackend-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

### 3. Docker Deployment (Example)

Create a `Dockerfile`:

```dockerfile
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/RoomieRadarBackend-0.0.1-SNAPSHOT.jar app.jar

# Create uploads directory
RUN mkdir -p /app/uploads

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

Build and run:

```powershell
# Build Docker image
docker build -t roomieradar-backend .

# Run with environment variables
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DB_URL=jdbc:postgresql://your-db-host:5432/roomieradar \
  -e DB_USERNAME=your_username \
  -e DB_PASSWORD=your_password \
  -e JWT_SECRET=your_jwt_secret \
  -e MAIL_USERNAME=your_email \
  -e MAIL_PASSWORD=your_password \
  -e ALLOWED_ORIGINS=https://your-frontend-domain.com \
  roomieradar-backend
```

## Health Checks

The application includes Spring Boot Actuator for health monitoring.

### Health Endpoint

**Development:**
```
GET http://localhost:8080/actuator/health
```

Response (development):
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 256000000000,
        "free": 128000000000,
        "threshold": 10485760
      }
    }
  }
}
```

**Production:**
```
GET https://your-api-domain.com/actuator/health
```

Response (production - limited details):
```json
{
  "status": "UP"
}
```

### Info Endpoint (Development Only)

```
GET http://localhost:8080/actuator/info
```

## Database Schema Management

### Development
- **Schema Mode**: `ddl-auto=update`
- Hibernate automatically updates database schema
- Convenient for rapid development

### Production
- **Schema Mode**: `ddl-auto=validate`
- Hibernate only validates existing schema
- **IMPORTANT**: You must manually update the database schema
- Use migration tools like Flyway or Liquibase for production migrations

### Manual Schema Updates

If you add new entities or fields:

1. Test in development (schema auto-updates)
2. Generate SQL from Hibernate logs
3. Review and apply SQL manually to production database
4. Or use a migration tool for versioned schema changes

## Security Best Practices

### JWT Secret
- **Minimum length**: 32 characters
- Use a cryptographically secure random string
- Generate one using: `openssl rand -base64 32`
- **NEVER** commit secrets to version control

### Database Password
- Use strong, unique passwords
- Consider using database connection pooling with credentials rotation
- Use managed database services with IAM authentication when possible

### CORS Configuration
- **Production**: Set `ALLOWED_ORIGINS` to your exact frontend domain
- Never use wildcards (`*`) in production
- Include protocol (https) in origins

### Email Credentials
- Use app-specific passwords for Gmail
- Consider using transactional email services (SendGrid, AWS SES)

## Troubleshooting

### Application Fails to Start

**Check Profile Activation:**
```powershell
# Verify profile in logs
# Look for: "The following profiles are active: prod"
```

**Missing Environment Variables:**
```
Error: Could not resolve placeholder 'DB_PASSWORD'
Solution: Set all required environment variables
```

### Database Connection Failures

**Check database URL format:**
```
Correct: jdbc:postgresql://localhost:5432/roomie_radar
Wrong: postgresql://localhost:5432/roomie_radar
```

**Verify database credentials:**
```powershell
# Test PostgreSQL connection
psql -h localhost -U postgres -d roomie_radar
```

### File Upload Issues

**Check upload directory permissions:**
```powershell
# Ensure directory exists and is writable
# Development: ./uploads
# Production: /app/uploads or $FILE_UPLOAD_DIR
```

### CORS Errors

**Update allowed origins:**
```powershell
# Make sure frontend URL matches exactly
$env:ALLOWED_ORIGINS="https://your-frontend.com"

# Multiple origins (comma-separated)
$env:ALLOWED_ORIGINS="https://app.com,https://www.app.com"
```

### Actuator Health Check Returns 404

**Verify Actuator is enabled:**
- Check `pom.xml` includes `spring-boot-starter-actuator`
- Check application properties include `management.endpoints.web.exposure.include=health`

## Additional Resources

- [Spring Boot Profiles Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.profiles)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [PostgreSQL Connection Strings](https://www.postgresql.org/docs/current/libpq-connect.html#LIBPQ-CONNSTRING)
- [JWT Best Practices](https://tools.ietf.org/html/rfc8725)

## Support

For issues or questions about deployment, please refer to:
- Application logs in `/var/log/roomieradar/` (production)
- Console output (development)
- Health check endpoint status
