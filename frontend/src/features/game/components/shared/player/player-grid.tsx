import { PropsWithChildren } from "react"

export const PlayerGrid = ({ children }: PropsWithChildren) => {
  return (
    <div className="grid grid-cols-[repeat(auto-fill,minmax(8rem,1fr))] gap-2">
      {children}
    </div>
  )
}
