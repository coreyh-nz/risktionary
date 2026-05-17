import { BackLink } from "@/components/back-button"
import {
  PageDescription,
  PageHeader,
  PageTitle,
} from "@/components/common/typography"
import { Container } from "@/components/layout/container"
import { PageSection } from "@/components/layout/page-section"
import { getWord } from "@/features/words/api/words.api"
import { UpdateWordSection } from "@/features/words/components/update-word-section"

interface UpdateWordPageParams {
  params: Promise<{
    id: string
  }>
}

const UpdateWordPage = async ({ params }: UpdateWordPageParams) => {
  const id = (await params).id
  const word = await getWord(id)

  return (
    <Container size="3xl">
      <PageSection className="flex-1">
        <BackLink />
        <PageHeader>
          <PageTitle>Update Word</PageTitle>
          <PageDescription className="mt-2">
            Update a word for players to draw.
          </PageDescription>
        </PageHeader>

        <UpdateWordSection word={word} />
      </PageSection>
    </Container>
  )
}

export default UpdateWordPage
