"use client"

import { ROUTES } from "@/lib/routes"
import { useRouter } from "next/navigation"
import { toast } from "sonner"
import { useCreateWord } from "../hooks/use-create-word"
import { WordFormValues } from "../hooks/use-word-form"
import { WordFormSection } from "./word-form-section"

export const CreateWordSection = () => {
  const { createWord, isLoading } = useCreateWord()
  const router = useRouter()

  const onSubmit = async ({
    word,
    synonyms,
    descriptionText,
    descriptionContent,
  }: WordFormValues) => {
    const response = await createWord({
      value: word,
      synonyms: synonyms.map((s) => s.value),
      descriptionText: descriptionText,
      descriptionContent: descriptionContent,
    })
    if (response.ok) {
      toast("Successfully created word.")
      router.push(ROUTES.WORDS.INDEX)
    }
  }

  return (
    <WordFormSection
      submitButtonText="Create Word"
      onSubmit={onSubmit}
      isLoading={isLoading}
    />
  )
}
