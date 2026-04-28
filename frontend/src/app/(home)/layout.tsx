import { Navbar } from "@/components/layout/navbar"
import { PropsWithChildren } from "react"

const HomeLayout = ({ children }: PropsWithChildren) => {
  return (
    <div className="relative flex min-h-svh items-center justify-center p-6">
      <Navbar />
      <div className="relative flex w-full max-w-lg flex-col gap-8">
        {children}
      </div>
    </div>
  )
}

export default HomeLayout
