import { Container } from "@/components/layout/container"
import { PropsWithChildren } from "react"

const GameCreateLayout = ({ children }: PropsWithChildren) => {
  return <Container size="3xl">{children}</Container>
}

export default GameCreateLayout
