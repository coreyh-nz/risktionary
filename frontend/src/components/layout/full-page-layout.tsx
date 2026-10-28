import { PropsWithChildren } from "react"

export const FullPageLayout = ({ children }: PropsWithChildren) => {
  return (
    <div className="flex flex-1 flex-col min-h-0 min-w-0 p-4">{children}</div>
  )
}
