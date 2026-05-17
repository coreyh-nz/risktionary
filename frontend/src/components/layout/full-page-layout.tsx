import { PropsWithChildren } from "react"

export const FullPageLayout = ({ children }: PropsWithChildren) => {
  return <div className="flex w-full">{children}</div>
}
