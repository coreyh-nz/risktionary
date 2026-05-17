import { useApi } from "@/hooks/use-api"
import { API_ROUTES } from "@/lib/api/api-routes"

export const useDeleteWord = (id: string) => {
  const { request, ...rest } = useApi<void, void>(
    API_ROUTES.V1.WORDS.individual(id),
    {
      method: "DELETE",
      credentials: "include",
    }
  )

  const deleteWord = async () => await request()

  return {
    deleteWord,
    ...rest,
  }
}
