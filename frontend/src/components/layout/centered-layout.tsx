import { type VariantProps } from "class-variance-authority"
import { PropsWithChildren } from "react"
import { Container, containerVariants } from "./container"

interface CenteredLayoutProps
  extends PropsWithChildren, VariantProps<typeof containerVariants> {}

export const CenteredLayout = ({
  children,
  size = "lg",
}: CenteredLayoutProps) => {
  return (
    <div className="flex flex-1 items-center justify-center">
      <Container size={size}>{children}</Container>
    </div>
  )
}
