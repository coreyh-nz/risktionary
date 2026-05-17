import { Geist_Mono, Outfit } from "next/font/google"

import "./globals.css"
import { cn } from "@/lib/utils"
import { Toaster } from "@/components/ui/sonner"
import { PropsWithChildren } from "react"

const outfit = Outfit({ subsets: ["latin"], variable: "--font-sans" })

const fontMono = Geist_Mono({
  subsets: ["latin"],
  variable: "--font-mono",
})

const RootLayout = async ({ children }: PropsWithChildren) => {
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

          <div className="relative z-10 min-h-svh flex flex-col">
            {children}
          </div>
        </div>
        <Toaster />
      </body>
    </html>
  )
}

export default RootLayout
