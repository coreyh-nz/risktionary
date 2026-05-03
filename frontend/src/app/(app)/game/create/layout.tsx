import { PropsWithChildren } from "react"
import { CenteredLayout } from "@/components/layout/centered-layout"

const GameCreateLayout = ({ children }: PropsWithChildren) => {
  return <CenteredLayout size="3xl">{children}</CenteredLayout>
}

export default GameCreateLayout
