# Risktionary Frontend

A Next.js frontend application for Risktionary, built with TypeScript, Tailwind CSS, and shadcn/ui.

---

## Project Structure

The codebase follows a feature-first architecture. Logic is organized by domain rather than by technical layer, so
everything related to a feature (components, hooks, types, API calls) lives together under `features/`.

```
src/
├── app/                  # Next.js routing
├── components/           # Shared UI primitives
├── features/             # Domain features
│   ├── auth/
│   └── game/
├── hooks/                # Shared React hooks used across multiple features
├── lib/                  # Framework-agnostic utilities (API, config, routes)
└── providers/            # React context providers
```

### Features

Each feature is self-contained and follows a consistent internal structure:

```
features/<name>/
├── components/   # UI components scoped to this feature
├── hooks/        # React hooks scoped to this feature
├── stores/       # Zustand stores, slices, and selectors
├── socket/       # WebSocket subscription setup
└── types/        # TypeScript types scoped to this feature
```

---

## Getting Started

### Development

Run the app locally with hot reloading.

```bash
npm install
npm run dev
```

The app runs at http://localhost:3000

### Production (Docker)

Build and run the app in a Docker container.

1. Build the image

    ```bash
    docker build -t frontend .
    ```

2. Run the container

    ```bash
    docker run -p 3000:3000 frontend
    ```

   To run in detached mode (background):

    ```bash
    docker run -p 3000:3000 -d frontend
    ```

The app runs at http://localhost:3000

### Docker Compose

Builds the image and starts the container in one step. Useful for running alongside other services.

```bash
docker compose up --build
```

To run in detached mode (background):

```bash
docker compose up --build -d
```

The app runs at http://localhost:3000
