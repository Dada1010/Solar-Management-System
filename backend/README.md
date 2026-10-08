# Backend Setup

Spring Boot REST API for Aditya Solar Management. The dev profile listens on port `8081`; the prod profile listens on port `8082`. The API uses MySQL and Liquibase.

## Requirements

- Java 21
- MySQL 8.x

## Local Development

1. Create a MySQL database for the application.
2. Set local database connection values in `src/main/resources/application-dev.properties` (`spring.datasource.url`, `spring.datasource.username`, and `spring.datasource.password`). Keep credentials private; do not commit real passwords or JWT secrets.
3. From the `backend` directory, start the API:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"
```

The dev API base URL is `http://localhost:8081/api/v1`. API documentation is available at `http://localhost:8081/swagger-ui/index.html` when the dev application is running. The prod profile listens on `http://localhost:8082` unless overridden by `SERVER_PORT`.

To run the local profile, which inherits dev settings and binds to `localhost`, use:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
```

## Production Configuration

Production settings are in `src/main/resources/application-prod.properties`. Configure the database URL, username, password, JWT secret and expiration, CORS allowed origins, server port, Liquibase, and JPA settings for the deployment environment. Prefer deployment environment variables or a secret manager for credentials and secrets; Spring environment variables override profile-file values. Never use development credentials or a weak/default JWT secret in production.

Activate the production profile when starting the app:

```powershell
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=prod"
```

For a packaged deployment, build and run the JAR:

```powershell
.\mvnw.cmd clean package
$env:SPRING_PROFILES_ACTIVE = "prod"
java -jar .\target\aditya-solar-management-api-0.0.1-SNAPSHOT.jar
```

Set deployment-specific values outside source control. Relevant environment variables include `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, `APP_SECURITY_JWTSECRET`, `APP_SECURITY_JWTEXPIRATIONMS`, `APP_CORS_ALLOWEDORIGIN`, and `SERVER_PORT`.

## Database Changes

Liquibase owns schema changes. Add a uniquely identified changeset under `src/main/resources/db/changelog/changes` and include it in `db.changelog-master.xml`. Do not edit a changeset after it has been applied to a shared database. Hibernate validates the schema at startup.