import { useApi } from "@/hooks/use-api"
import { API_ROUTES } from "@/lib/api/api-routes"

interface CreateWordRequest {
  value: string
  synonyms: string[]
  descriptionText: string
  descriptionContent: string
}

export const useCreateWord = () => {
  const { request, ...rest } = useApi<void, CreateWordRequest>(
    API_ROUTES.V1.WORDS.BASE,
    {
      method: "POST",
      credentials: "include",
    }
  )

  const createWord = async (body: CreateWordRequest) => await request({ body })

  return {
    createWord,
    ...rest,
  }
}
