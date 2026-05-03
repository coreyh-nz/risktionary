"use client"

import { useAuth } from "@/providers/auth-provider"
import { API_ROUTES } from "@/lib/api/api-routes"
import { buttonVariants } from "@/components/ui/button"
import { GoogleIcon } from "@/features/auth/components/icons/google-icon"
import { MicrosoftIcon } from "@/features/auth/components/icons/microsoft-icon"
import { config } from "@/lib/config"
import { useEffect, useState } from "react"
import { OAuthButton } from "@/features/auth/components/oauth-button"
import Link from "next/link"
import { ROUTES } from "@/lib/routes"

export const AuthSection = () => {
  const { user } = useAuth()
  const [redirectUrl, setRedirectUrl] = useState<string | null>(null)

  useEffect(() => {
    // react renders the component server side then hydrates client side
    // but, we don't have access to window when it renders server side
    // if we render based on if window is defined or not, then we get hydration error
    // so just re-render after it has hydrated
    // eslint-disable-next-line react-hooks/set-state-in-effect
    setRedirectUrl(window.location.href)
  }, [])

  const getOAuthHref = (route: string) => {
    const base = `${config.apiUrl}${route}`
    return redirectUrl
      ? `${base}?redirectUrl=${encodeURIComponent(redirectUrl)}`
      : base
  }

  if (user) {
    return (
      <div className="flex w-full justify-center">
        <div className="flex w-full max-w-sm items-center justify-center gap-3">
          <div className="text-center text-sm text-muted-foreground">
            Ready to start a new game?
            <Link
              href={ROUTES.GAME.CREATE}
              className={buttonVariants({ variant: "link" })}
            >
              Create Game
            </Link>
          </div>
        </div>
      </div>
    )
  }

  return (
    <>
      <div className="relative">
        <div className="absolute inset-0 flex items-center">
          <div className="w-full border-t border-border" />
        </div>
        <div className="relative flex justify-center text-sm">
          <span className="px-4 font-medium text-muted-foreground">
            or sign in to track progress
          </span>
        </div>
      </div>

      <div className="flex w-full gap-4">
        <OAuthButton href={getOAuthHref(API_ROUTES.V1.OAUTH.GOOGLE)}>
          <GoogleIcon />
          Google
        </OAuthButton>

        <OAuthButton href={getOAuthHref(API_ROUTES.V1.OAUTH.MICROSOFT)}>
          <MicrosoftIcon />
          Microsoft
        </OAuthButton>
      </div>
    </>
  )
}
