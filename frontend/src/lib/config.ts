export const config = {
  isDev: process.env.NODE_ENV !== "production",
  apiUrl: process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080",
  wsUrl: process.env.NEXT_PUBLIC_WS_URL ?? "ws://localhost:8080/ws",
} as const
