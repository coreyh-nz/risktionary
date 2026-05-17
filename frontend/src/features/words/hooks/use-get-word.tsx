import { useApi } from "@/hooks/use-api"
import { API_ROUTES } from "@/lib/api/api-routes"
import { Word } from "../types/word"

interface GetWordResponse {
  words: Word[]
}

export const useGetWord = (id: string) => {
  const { request, ...rest } = useApi<GetWordResponse, void>(
    API_ROUTES.V1.WORDS.individual(id),
    {
      method: "GET",
      credentials: "include",
    }
  )

  return {
    getWord: request,
    ...rest,
  }
}
