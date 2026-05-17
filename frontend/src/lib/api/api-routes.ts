export const API_ROUTES = {
  V1: {
    USER: {
      ME: "/v1/user/me",
    },

    AUTH: {
      LOGOUT: "/v1/auth/logout",
    },

    OAUTH: {
      GOOGLE: "/v1/oauth/google",
      MICROSOFT: "/v1/oauth/microsoft",
    },

    GAME: {
      CREATE: "/v1/game/create",
      JOIN: "/v1/game/join",
    },

    WORDS: {
      BASE: "/v1/words",
      individual: (id: string) => `/v1/words/${id}`,
    },
  },
}
