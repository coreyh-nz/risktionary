export const config = {
  apiUrl: process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080",
  persistGameSession: process.env.NEXT_PUBLIC_PERSIST_GAME_SESSION ?? false,
} as const
