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
