import { cookies } from "next/headers"
import { apiRequest } from "@/lib/api/request"
import { API_ROUTES } from "@/lib/api/api-routes"
import { User } from "@/features/auth/types/user"

export async function getCurrentUser(): Promise<User | null> {
  const cookieHeader = (await cookies()).toString()
  const response = await apiRequest<User>(API_ROUTES.V1.USER.ME, {
    headers: {
      Cookie: cookieHeader,
    },
  })

  if (!response.ok) {
    // bad gateway or service unavailable
    if (response.error.status === 502 || response.error.status === 503) {
      throw Error(response.error.message)
    }
    return null
  }
  return response.data
}
