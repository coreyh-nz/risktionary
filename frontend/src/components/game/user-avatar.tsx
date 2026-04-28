"use client"

import { Avatar, AvatarFallback } from "@/components/ui/avatar"
import { cn } from "@/lib/utils"

type UserAvatarProps = {
  displayName?: string
  firstName?: string
  lastName?: string
  className?: string
}

const COLORS = [
  "bg-red-500",
  "bg-orange-500",
  "bg-amber-500",
  "bg-yellow-500",
  "bg-lime-500",
  "bg-green-500",
  "bg-emerald-500",
  "bg-teal-500",
  "bg-cyan-500",
  "bg-sky-500",
  "bg-blue-500",
  "bg-indigo-500",
  "bg-violet-500",
  "bg-purple-500",
  "bg-pink-500",
]

const getInitials = (props: UserAvatarProps) => {
  if (props.displayName) {
    const parts = props.displayName.trim().split(" ").filter(Boolean)

    if (parts.length === 1) {
      return parts[0][0].toUpperCase()
    }

    return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase()
  }

  if (props.firstName || props.lastName) {
    const first = props.firstName?.[0] ?? ""
    const last = props.lastName?.[0] ?? ""
    return (first + last).toUpperCase()
  }

  return "?"
}

const getColorFromInitials = (initials: string) => {
  let hash = 0
  for (let i = 0; i < initials.length; i++) {
    hash = initials.charCodeAt(i) + ((hash << 5) - hash)
  }

  const index = Math.abs(hash) % COLORS.length
  return COLORS[index]
}

export const UserAvatar = (props: UserAvatarProps) => {
  const initials = getInitials(props)
  const colorClass = getColorFromInitials(initials)

  return (
    <Avatar className={cn("h-8 w-8", colorClass, props.className)}>
      <AvatarFallback className="bg-transparent text-xs font-semibold text-white">
        {initials}
      </AvatarFallback>
    </Avatar>
  )
}
