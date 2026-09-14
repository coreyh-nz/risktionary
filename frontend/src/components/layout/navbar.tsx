"use client"
import { Logo } from "@/components/common/logo"
import { UserAvatar } from "@/components/common/user-avatar"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuGroup,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import { useLogout } from "@/features/auth/hooks/use-logout"
import { ROUTES } from "@/lib/routes"
import { cn } from "cn"

import { useAuth } from "@/providers/auth-provider"
import { LogOut } from "lucide-react"
import Link from "next/link"
import { usePathname } from "next/navigation"
import { buttonVariants } from "../ui/button"

interface NavItem {
  label: string
  href: string
  requireAuthenticated: boolean
}

const NAV_ITEMS: NavItem[] = [
  {
    label: "Manage Words",
    href: ROUTES.WORDS.INDEX,
    requireAuthenticated: true,
  },
  {
    label: "Create Game",
    href: ROUTES.GAME.CREATE,
    requireAuthenticated: true,
  },
]

export const Navbar = () => {
  const { user } = useAuth()
  const { logout } = useLogout()
  const pathname = usePathname()

  const isActive = (href: string) =>
    pathname === href || pathname.startsWith(href + "/")

  return (
    <nav className="sticky top-0 z-50 border-b border-border bg-background/95 shadow-sm backdrop-blur-sm">
      <div className="mx-auto max-w-5xl px-4">
        <div className="flex h-14 items-center justify-between">
          <Logo size="md" />

          <div className="flex items-center gap-1">
            {NAV_ITEMS.filter((item) => !item.requireAuthenticated || user).map(
              (item) => (
                <Link
                  key={item.href}
                  href={item.href}
                  className={cn(
                    buttonVariants({ variant: "ghost", size: "sm" }),
                    "relative text-muted-foreground transition-colors hover:text-foreground",
                    isActive(item.href) &&
                      "text-foreground after:absolute after:inset-x-1 after:-bottom-4 after:h-px after:bg-foreground"
                  )}
                >
                  {item.label}
                </Link>
              )
            )}
          </div>

          {user && (
            <DropdownMenu>
              <DropdownMenuTrigger className="flex items-center gap-2.5 rounded-lg p-1 transition-colors outline-none hover:bg-accent">
                <div className="hidden text-right sm:block">
                  <p className="text-sm leading-tight font-medium text-foreground">
                    {user.displayName}
                  </p>
                  <p className="text-xs leading-tight text-muted-foreground">
                    {user.email}
                  </p>
                </div>
                <UserAvatar
                  firstName={user.firstName}
                  lastName={user.lastName}
                  displayName={user.displayName}
                />
              </DropdownMenuTrigger>
              <DropdownMenuContent align="end" className="w-48">
                <DropdownMenuGroup>
                  <DropdownMenuLabel className="font-normal">
                    <p className="font-medium">{user.displayName}</p>
                    <p className="text-xs text-muted-foreground">
                      {user.email}
                    </p>
                  </DropdownMenuLabel>
                  <DropdownMenuSeparator />
                  <DropdownMenuItem
                    variant="destructive"
                    onClick={() => logout()}
                  >
                    <LogOut />
                    Sign out
                  </DropdownMenuItem>
                </DropdownMenuGroup>
              </DropdownMenuContent>
            </DropdownMenu>
          )}
        </div>
      </div>
    </nav>
  )
}
