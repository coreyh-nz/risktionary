import { useFetchLogout } from "@/hooks/api/auth/use-fetch-logout"
import { useAuth } from "@/provider/AuthProvider"
import { toast } from "sonner"

interface UseLogoutReturn {
  logout: () => Promise<void>
}

export const useLogout = (): UseLogoutReturn => {
  const { setUser } = useAuth()
  const { fetchLogout } = useFetchLogout()

  const logout = async () => {
    const response = await fetchLogout()
    if (!response.ok) {
      toast(response.error.errorCode ?? "Failed to logout")
      return
    }
    setUser(null)
  }

  return { logout }
}
