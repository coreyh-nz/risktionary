import { ServiceUnavailablePage } from "@/components/error/service-unavailable-page"
import { CenteredLayout } from "@/components/layout/centered-layout"
import { Navbar } from "@/components/layout/navbar"
import { getCurrentUser } from "@/features/auth/api/auth.api"
import { User } from "@/features/auth/types/user"
import { AuthProvider } from "@/providers/auth-provider"
import { PropsWithChildren } from "react"

const AppLayout = async ({ children }: PropsWithChildren) => {
  let user: User | null = null

  // getCurrentUser throws error if the fetch request throws which means the
  // backend is offline
  try {
    user = await getCurrentUser()
  } catch {
    return (
      <CenteredLayout>
        <ServiceUnavailablePage />
      </CenteredLayout>
    )
  }

  return (
    <AuthProvider user={user}>
      <div className="flex min-h-svh flex-col">
        <Navbar />
        <main className="flex flex-1">{children}</main>
      </div>
    </AuthProvider>
  )
}

export default AppLayout
