import { Toaster } from "@/components/ui/sonner"
import { cn } from "@/lib/utils"
import { Geist_Mono, Outfit } from "next/font/google"
import { PropsWithChildren } from "react"
import "./globals.css"

import { ServiceUnavailablePage } from "@/components/error/service-unavailable-page"
import { Navbar } from "@/components/layout/navbar"
import { AuthProvider } from "@/providers/auth-provider"

import { getCurrentUser } from "@/features/auth/api/auth.api"
import { User } from "@/features/auth/types/user"

const outfit = Outfit({ subsets: ["latin"], variable: "--font-sans" })
const fontMono = Geist_Mono({ subsets: ["latin"], variable: "--font-mono" })

const AppShell = async ({ children }: PropsWithChildren) => {
  let user: User | null = null
  let isOffline = false

  try {
    user = await getCurrentUser()
  } catch {
    isOffline = true
  }

  if (isOffline) {
    return (
      <>
        <main className="flex flex-1 items-center justify-center">
          <ServiceUnavailablePage />
        </main>
      </>
    )
  }

  return (
    <AuthProvider user={user}>
      <Navbar />
      <main className="flex-1 min-h-0 overflow-y-auto flex flex-col">
        <div className="flex flex-col flex-1 min-h-0">{children}</div>
      </main>
    </AuthProvider>
  )
}

const RootLayout = ({ children }: PropsWithChildren) => {
  return (
    <html
      lang="en"
      suppressHydrationWarning
      className={cn(
        "antialiased",
        fontMono.variable,
        "font-sans",
        outfit.variable
      )}
    >
      <body>
        <div className="relative bg-background">
          <div
            className="pointer-events-none fixed inset-0 z-10"
            style={{
              background: `radial-gradient(
                circle at center,
                oklch(from var(--primary) calc(l * 0.85) calc(c * 0.8) h / 0.12) 0%,
                oklch(from var(--primary) calc(l * 0.8) calc(c * 0.75) h / 0.08) 30%,
                oklch(from var(--primary) calc(l * 0.75) calc(c * 0.7) h / 0.04) 60%,
                transparent 80%
              )`,
            }}
          />

          <div className="relative z-10 h-svh flex flex-col">
            <AppShell>{children}</AppShell>
          </div>
        </div>

        <Toaster />
      </body>
    </html>
  )
}

export default RootLayout
