import { GameSessionPlayerView } from "@/features/game/types/game"
import { useApi } from "@/hooks/use-api"
import { API_ROUTES } from "@/lib/api/api-routes"

interface JoinGameRequest {
  code: string
  displayName: string
}

interface JoinGameResponse {
  session: GameSessionPlayerView
  ticket: string
  playerId: string
  displayName: string
  feedbackEnabled: boolean
}

export const useJoinGame = () => {
  const { request, ...rest } = useApi<JoinGameResponse, JoinGameRequest>(
    API_ROUTES.V1.GAME.JOIN,
    {
      method: "POST",
      credentials: "include",
    }
  )

  const joinGame = async (body: JoinGameRequest) => await request({ body })

  return {
    joinGame,
    ...rest,
  }
}
