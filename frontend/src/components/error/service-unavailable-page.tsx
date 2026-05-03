import { ErrorPage } from "@/components/error/error-page"

export const ServiceUnavailablePage = () => {
  return (
    <ErrorPage
      title="Service Unavailable"
      description="Things aren't quite right on our end. Give it a minute and try again."
    />
  )
}
