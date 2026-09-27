import { useGameReset } from "@/features/game/stores/game-store-selectors"
import { useApi } from "@/hooks/use-api"
import { API_ROUTES } from "@/lib/api/api-routes"
import { useAuth } from "@/providers/auth-provider"
import { toast } from "sonner"

interface UseLogoutReturn {
  logout: () => Promise<void>
}

export const useLogout = (): UseLogoutReturn => {
  const { request } = useApi<void, void>(API_ROUTES.V1.AUTH.LOGOUT, {
    method: "POST",
    credentials: "include",
  })
  const { setUser } = useAuth()
  const resetGame = useGameReset()

  const logout = async () => {
    const response = await request()
    if (!response.ok) {
      toast(response.error.errorCode ?? "Failed to logout")
      return
    }
    setUser(null)
    resetGame()
  }

  return { logout }
}
