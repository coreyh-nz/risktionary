import { cookies } from "next/headers"
import { apiRequest } from "@/lib/api/request"
import { API_ROUTES } from "@/lib/api/api-routes"
import { User } from "@/types/user"

export async function getCurrentUser(): Promise<User | null> {
  const cookieHeader = (await cookies()).toString()
  const response = await apiRequest<User>(API_ROUTES.V1.USER.ME, {
    headers: {
      Cookie: cookieHeader,
    },
  })
  return response.ok ? response.data : null
}
