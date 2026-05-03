import { ErrorPage } from "@/components/error/error-page"
import { LinkButton } from "@/components/common/link-button"
import { ROUTES } from "@/lib/routes"
import { CenteredLayout } from "@/components/layout/centered-layout"

const NotFoundPage = () => {
  return (
    <CenteredLayout>
      <ErrorPage
        title="Page Not Found"
        description="The page you're looking for doesn't exist or has been moved."
      >
        <LinkButton href={ROUTES.HOME} variant="outline">
          Go home
        </LinkButton>
      </ErrorPage>
    </CenteredLayout>
  )
}

export default NotFoundPage
