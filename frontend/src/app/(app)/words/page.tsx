"use client"

import {
  PageDescription,
  PageHeader,
  PageTitle,
} from "@/components/common/typography"
import { Container } from "@/components/layout/container"
import { PageSection } from "@/components/layout/page-section"
import { buttonVariants } from "@/components/ui/button"
import { WordsList } from "@/features/words/components/words-list"
import { WordsProvider } from "@/features/words/provider/words-provider"
import { ROUTES } from "@/lib/routes"
import { Plus } from "lucide-react"
import Link from "next/link"

const WordsPage = () => {
  return (
    <Container size="3xl">
      <PageSection className="flex-1">
        <div className="flex justify-between items-center">
          <PageHeader>
            <PageTitle>Word Library</PageTitle>
            <PageDescription className="mt-2">
              Create and manage words for games.
            </PageDescription>
          </PageHeader>
          <Link href={ROUTES.WORDS.CREATE} className={buttonVariants()}>
            <Plus />
            Add Word
          </Link>
        </div>

        <WordsProvider>
          <WordsList />
        </WordsProvider>
      </PageSection>
    </Container>
  )
}

export default WordsPage
