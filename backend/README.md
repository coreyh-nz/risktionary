# Risktionary Backend

A Spring Boot backend application for Risktionary, built with Kotlin.

---

## Getting Started

### Development

Run the app locally with automatic recompilation on code changes.
```bash
./gradlew bootRun
```

The app runs at http://localhost:8080

### Production (Docker)

Build and run the app in a Docker container.

1. Build the image
    ```bash
    docker build -t backend .
    ```

2. Run the container
    ```bash
    docker run -p 8080:8080 backend
    ```

    To run in detached mode (background):
    ```bash
    docker run -p 8080:8080 -d backend
    ```

The app runs at http://localhost:8080

### Docker Compose

Builds the image and starts the container in one step. Useful for running alongside other services.
```bash
docker compose up --build
```

To run in detached mode (background):
```bash
docker compose up --build -d
```

The app runs at http://localhost:8080

---

## Environment Variables

### Profiles

The backend uses different configurations depending on the active Spring profile.

- **mariadb** - connects to an external MariaDB instance
- **h2** - uses an in‑memory H2 database (no environment variables required)

#### MariaDB Profile

When running with the `mariadb` profile, the following environment variables **must** be set:

| Variable          | Description                    |
|-------------------|--------------------------------|
| DATABASE_NAME     | Name of the MariaDB database   |
| DATABASE_USER     | Username for the database      |
| DATABASE_PASSWORD | Password for the database user |

#### H2 Profile

When running with the `h2` profile, the application uses an in‑memory H2 database.
No environment variables are required.
