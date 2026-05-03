import { Logo } from "@/components/common/logo"
import { ReactNode } from "react"

interface ErrorPageProps {
  title: string
  description: string
  children?: ReactNode
}

export const ErrorPage = ({ title, description, children }: ErrorPageProps) => {
  return (
    <div className="flex w-full max-w-lg flex-col gap-8">
      <Logo size="xl" className="justify-center" />
      <div className="flex flex-col items-center gap-4 text-center">
        <div className="flex flex-col gap-2">
          <h1 className="text-2xl font-semibold tracking-tight">{title}</h1>
          <p className="text-muted-foreground">{description}</p>
        </div>
        {children}
      </div>
    </div>
  )
}
