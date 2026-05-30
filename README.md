# School Management Authentication Module

Production-ready authentication module for a Spring Boot based school management backend.

## Stack

- Java 17
- Spring Boot 3
- Spring Security
- JWT
- MySQL
- JPA / Hibernate
- JUnit 5 / Mockito

## API Endpoints

- `POST /api/users` - register a user
- `POST /api/auth/tokens` - login and receive a JWT
- `GET /api/users/me` - get the current authenticated user
- `DELETE /api/auth/tokens` - logout by blacklisting the JWT until it expires

## Module Architecture

Use [docs/MODULE_ARCHITECTURE.md](docs/MODULE_ARCHITECTURE.md) when adding a new module. It explains the project layers, naming rules, build order, and includes copy-ready templates in `docs/module-template`.

## Parent Portal

Parent users can access their own portal data through `/api/parent-portal`. These endpoints are protected with the `PARENT` role and resolve the parent profile from the authenticated JWT user, so parents cannot request another parent's data by changing a URL parameter.

- `GET /api/parent-portal/profile` - current parent profile
- `GET /api/parent-portal/dashboard` - parent profile plus linked student summary
- `GET /api/parent-portal/students` - linked active students
- `GET /api/parent-portal/students/{studentId}` - linked student details
- `GET /api/parent-portal/students/{studentId}/subjects` - linked student's class subjects
- `GET /api/parent-portal/students/{studentId}/timetable` - linked student's class timetable

## Run

1. Create a MySQL database or allow auto-creation with the configured JDBC URL.
2. Set environment variables if needed:
   - `DB_URL`
   - `DB_USERNAME`
   - `DB_PASSWORD`
   - `JWT_SECRET`
   - `JWT_EXPIRATION_MS`
3. Start the application:

```bash
mvn spring-boot:run
```

## Test

```bash
mvn test
```

Integration tests use H2 with the `test` profile.
