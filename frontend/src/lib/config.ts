export const config = {
  isDev: process.env.NODE_ENV !== "production",
  apiUrl: process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080",
  wsUrl: process.env.NEXT_PUBLIC_WS_URL ?? "ws://localhost:8080/ws",
  // shown to players as the link to visit; the actual redirect destination
  // is server-only (see SURVEY_REDIRECT_URL in src/app/survey/page.tsx)
  surveyUrl: process.env.NEXT_PUBLIC_SURVEY_URL ?? "",
} as const
