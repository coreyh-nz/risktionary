import { PropsWithChildren } from "react"
import { CenteredLayout } from "@/components/layout/centered-layout"

const HomeLayout = ({ children }: PropsWithChildren) => {
  return <CenteredLayout>{children}</CenteredLayout>
}

export default HomeLayout
