import { useApi } from "@/hooks/use-api"
import { API_ROUTES } from "@/lib/api/api-routes"
import { CreateGameRequest, CreateGameResponse } from "../../types/lobby/api"

export const useCreateGame = () => {
  const { request, ...rest } = useApi<CreateGameResponse, CreateGameRequest>(
    API_ROUTES.V1.GAME.CREATE,
    {
      method: "POST",
      credentials: "include",
    }
  )

  const createGame = async (body: CreateGameRequest) => await request({ body })

  return {
    createGame,
    ...rest,
  }
}
