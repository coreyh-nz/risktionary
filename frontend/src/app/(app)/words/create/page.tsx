import { BackLink } from "@/components/back-button"
import {
  PageDescription,
  PageHeader,
  PageTitle,
} from "@/components/common/typography"
import { Container } from "@/components/layout/container"
import { PageSection } from "@/components/layout/page-section"
import { CreateWordSection } from "@/features/words/components/create-word-section"

const CreateWordPage = () => {
  return (
    <Container size="3xl">
      <PageSection className="flex-1">
        <BackLink />
        <PageHeader>
          <PageTitle>Create New Word</PageTitle>
          <PageDescription className="mt-2">
            Create a new word for players to draw.
          </PageDescription>
        </PageHeader>

        <CreateWordSection />
      </PageSection>
    </Container>
  )
}

export default CreateWordPage
