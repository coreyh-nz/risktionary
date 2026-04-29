import { useAuth } from "@/providers/auth-provider"
import { toast } from "sonner"
import { useApi } from "@/hooks/use-api"
import { API_ROUTES } from "@/lib/api/api-routes"

interface UseLogoutReturn {
  logout: () => Promise<void>
}

export const useLogout = (): UseLogoutReturn => {
  const { request } = useApi<void, void>(API_ROUTES.V1.AUTH.LOGOUT, {
    method: "POST",
    credentials: "include",
  })
  const { setUser } = useAuth()

  const logout = async () => {
    const response = await request()
    if (!response.ok) {
      toast(response.error.errorCode ?? "Failed to logout")
      return
    }
    setUser(null)
  }

  return { logout }
}
