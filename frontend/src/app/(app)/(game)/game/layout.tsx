import { Navbar } from "@/components/layout/navbar"
import { PropsWithChildren } from "react"

const GameLayout = ({ children }: PropsWithChildren) => {
  return (
    <div className="relative flex min-h-svh items-center justify-center p-6">
      <Navbar />
      <div className="relative flex w-full max-w-3xl flex-col gap-8">
        {children}
      </div>
    </div>
  )
}

export default GameLayout
