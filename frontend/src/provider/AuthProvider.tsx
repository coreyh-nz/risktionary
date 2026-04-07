"use client"

import { createContext, PropsWithChildren, useContext, useState } from "react"
import { User } from "@/types/user"

interface AuthContextValue {
  user: User | null
  setUser: (user: User | null) => void
}

interface AuthProviderProps extends PropsWithChildren {
  user: User | null
}

const AuthContext = createContext<AuthContextValue | null>(null)

export const AuthProvider = ({
  user: initialUser,
  children,
}: AuthProviderProps) => {
  const [user, setUser] = useState<User | null>(initialUser)

  return (
    <AuthContext.Provider value={{ user, setUser }}>
      {children}
    </AuthContext.Provider>
  )
}

export const useAuth = () => {
  const context = useContext(AuthContext)
  if (!context) throw new Error("useAuth must be used within an AuthProvider")
  return context
}
