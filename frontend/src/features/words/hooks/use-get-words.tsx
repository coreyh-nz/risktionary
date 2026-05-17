import { useApi } from "@/hooks/use-api"
import { API_ROUTES } from "@/lib/api/api-routes"
import { WordSummary } from "../types/word"

interface GetWordsResponse {
  words: WordSummary[]
}

export const useGetWords = () => {
  const { request, ...rest } = useApi<GetWordsResponse, void>(
    API_ROUTES.V1.WORDS.BASE,
    {
      method: "GET",
      credentials: "include",
    }
  )

  return {
    getWords: request,
    ...rest,
  }
}
