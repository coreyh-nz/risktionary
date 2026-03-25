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