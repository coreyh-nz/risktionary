import { useApi } from "@/hooks/use-api"
import { API_ROUTES } from "@/lib/api/api-routes"

interface UpdateWordRequest {
  value: string
  synonyms: string[]
  descriptionText: string
  descriptionContent: string
}

export const useUpdateWord = (id: string) => {
  const { request, ...rest } = useApi<void, UpdateWordRequest>(
    API_ROUTES.V1.WORDS.individual(id),
    {
      method: "PUT",
      credentials: "include",
    }
  )

  const updateWord = async (body: UpdateWordRequest) => await request({ body })

  return {
    updateWord,
    ...rest,
  }
}
