import { useApi } from "@/hooks/api/use-api"
import { API_ROUTES } from "@/lib/api/api-routes"
import { ApiResponse } from "@/lib/api/request"

interface UseFetchLogoutReturn {
  fetchLogout: () => Promise<ApiResponse<void>>
}

export const useFetchLogout = (): UseFetchLogoutReturn => {
  const { request } = useApi<void, void>(API_ROUTES.V1.AUTH.LOGOUT, {
    method: "POST",
    credentials: "include",
  })

  return {
    fetchLogout: () => request(),
  }
}
