import { WordReviewView } from "@/features/game/types/round/phase/word-review/word"
import { useApi } from "@/hooks/use-api"
import { API_ROUTES } from "@/lib/api/api-routes"
import { useCallback } from "react"

interface CurrentGameRoundWordResponse {
  word: WordReviewView
}

const REQUEST_OPTIONS = {
  method: "GET",
  credentials: "include",
} as const

export const useGetCurrentGameRoundWord = (gameId: string) => {
  const { request, ...rest } = useApi<CurrentGameRoundWordResponse, void>(
    API_ROUTES.V1.GAME.currentRoundWord(gameId),
    REQUEST_OPTIONS
  )

  const getCurrentGameRoundWord = useCallback(() => request(), [request])

  return {
    getCurrentGameRoundWord,
    ...rest,
  }
}
