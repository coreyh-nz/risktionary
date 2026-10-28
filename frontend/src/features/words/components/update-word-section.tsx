"use client"

import { ROUTES } from "@/lib/routes"
import { useRouter } from "next/navigation"
import { toast } from "sonner"
import { useUpdateWord } from "../hooks/use-update-word"
import { WordFormValues } from "../hooks/use-word-form"
import { Word } from "../types/word"
import { WordFormSection } from "./word-form-section"

interface UpdateWordSectionProps {
  word: Word
}

export const UpdateWordSection = ({ word }: UpdateWordSectionProps) => {
  const { updateWord, isLoading } = useUpdateWord(word.id)
  const router = useRouter()

  const onSubmit = async ({
    word,
    synonyms,
    descriptionText,
    descriptionContent,
  }: WordFormValues) => {
    const response = await updateWord({
      value: word,
      synonyms: synonyms.map((s) => s.value),
      descriptionText: descriptionText,
      descriptionContent: descriptionContent,
    })
    if (response.ok) {
      toast("Successfully updated word.")
      router.push(ROUTES.WORDS.INDEX)
    }
  }

  return (
    <WordFormSection
      word={word}
      submitButtonText="Update Word"
      onSubmit={onSubmit}
      isLoading={isLoading}
    />
  )
}
