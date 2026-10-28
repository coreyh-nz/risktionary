export const ROUTES = {
  HOME: "/",
  GAME: {
    CREATE: "/game/create",
    PLAY: "/game/play",
  },
  WORDS: {
    INDEX: "/words",
    CREATE: "/words/create",
    edit: (id: string) => `/words/${id}/edit`,
  },
} as const
