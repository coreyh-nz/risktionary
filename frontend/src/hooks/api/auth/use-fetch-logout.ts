import { useApi } from "@/hooks/api/use-api"
import { ROUTES } from "@/lib/api/routes"
import { ApiResponse } from "@/lib/api/request"

interface UseFetchLogoutReturn {
  fetchLogout: () => Promise<ApiResponse<void>>
}

export const useFetchLogout = (): UseFetchLogoutReturn => {
  const { request } = useApi<void, void>(ROUTES.V1.AUTH.LOGOUT, {
    method: "POST",
    credentials: "include",
  })

  return {
    fetchLogout: () => request(),
  }
}
