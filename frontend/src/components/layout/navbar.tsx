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
import { cn } from "@/lib/utils"
import { useAuth } from "@/providers/auth-provider"
import { LogOut } from "lucide-react"
import Link from "next/link"
import { buttonVariants } from "../ui/button"

export const Navbar = () => {
  const { user } = useAuth()
  const { logout } = useLogout()

  return (
    <nav className="fixed inset-x-0 top-0 z-50">
      <div className="mx-auto max-w-5xl px-4 py-3">
        <div className="grid h-14 grid-cols-3 items-center rounded-2xl border border-border/50 bg-card/80 px-4 py-2 shadow-[0_0_20px_rgba(0,0,0,0.3)] backdrop-blur-xl">
          <div className="flex items-center">
            <Logo size="md" />
          </div>

          <div className="flex items-center justify-center">
            {user && (
              <Link
                href={ROUTES.WORDS.INDEX}
                className={cn(buttonVariants({ variant: "ghost" }))}
              >
                Manage Words
              </Link>
            )}
          </div>

          <div className="flex items-center justify-end">
            {user && (
              <DropdownMenu>
                <DropdownMenuTrigger className="flex items-center gap-2.5 rounded-xl p-1 transition-colors outline-none hover:bg-accent">
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
      </div>
    </nav>
  )
}
