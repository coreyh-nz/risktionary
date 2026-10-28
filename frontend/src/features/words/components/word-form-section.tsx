"use client"

import { FormInput } from "@/components/common/form"
import { LinkButton } from "@/components/common/link-button"
import { CardStack } from "@/components/layout/card-stack"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card"
import { Field, FieldError } from "@/components/ui/field"
import { Input } from "@/components/ui/input"
import { Spinner } from "@/components/ui/spinner"
import { ROUTES } from "@/lib/routes"
import { Plus, X } from "lucide-react"
import { memo, useCallback, useState } from "react"
import { useFieldArray } from "react-hook-form"
import { useWordForm, WordFormValues } from "../hooks/use-word-form"
import { Word } from "../types/word"
import { RichEditor } from "./editor/rich-editor"

interface WordFormSectionProps {
  word?: Word
  submitButtonText: string

  onSubmit: (values: WordFormValues) => Promise<void>
  isLoading?: boolean
}

export const WordFormSection = ({
  word,
  submitButtonText,
  onSubmit,
  isLoading,
}: WordFormSectionProps) => {
  const form = useWordForm(
    word && {
      word: word.value,
      synonyms: word.synonyms.map((s) => ({ value: s })),
      descriptionText: word.descriptionText,
      descriptionContent: word.descriptionContent,
    }
  )
  const synonyms = useFieldArray({
    control: form.control,
    name: "synonyms",
  })

  // returns true if it was added, false if not
  const handleAddSynonym = (synonym: string) => {
    if (synonyms.fields.find((s) => s.value == synonym)) return false
    synonyms.append({ value: synonym })
    return true
  }

  const handleRemoveSynonym = (synonymId: string) => {
    const index = synonyms.fields.findIndex((s) => s.id === synonymId)
    if (index !== -1) synonyms.remove(index)
  }

  const handleDescriptionChange = useCallback(
    (text: string, content: string) => {
      form.setValue("descriptionText", text, { shouldValidate: true })
      form.setValue("descriptionContent", content, { shouldValidate: true })
    },
    [form]
  )

  return (
    <form onSubmit={form.handleSubmit(onSubmit)}>
      <CardStack>
        <Card>
          <CardHeader>
            <CardTitle>Word</CardTitle>
            <CardDescription>
              The main word that players will try to draw and guess.
            </CardDescription>
          </CardHeader>
          <CardContent>
            <Field>
              <FormInput name="word" label={undefined} control={form.control} />
            </Field>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Synonyms/Close Matches</CardTitle>
            <CardDescription>
              Alternative words that will be accepted as correct answers.
            </CardDescription>
          </CardHeader>
          <CardContent>
            <Field orientation="horizontal">
              <AddSynonymForm onAddSynonym={handleAddSynonym} />
            </Field>
            {synonyms.fields.length > 0 && (
              <div className="mt-3 flex flex-wrap gap-2">
                {synonyms.fields.map((synonym) => (
                  <Badge key={synonym.id} variant="secondary">
                    {synonym.value}
                    <Button
                      variant="ghost"
                      size="icon-xs"
                      onClick={() => handleRemoveSynonym(synonym.id)}
                    >
                      <X />
                    </Button>
                  </Badge>
                ))}
              </div>
            )}
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Description</CardTitle>
            <CardDescription>
              A description to share with players after the word is guessed.
            </CardDescription>
          </CardHeader>
          <CardContent>
            <Field>
              <DescriptionField
                className={
                  form.formState.errors["descriptionText"] &&
                  "border-destructive ring-3 ring-destructive/20"
                }
                content={word && word.descriptionContent}
                onChange={handleDescriptionChange}
              />
              {form.formState.errors["descriptionText"]?.message && (
                <FieldError>
                  {form.formState.errors["descriptionText"].message}
                </FieldError>
              )}
            </Field>
          </CardContent>
        </Card>

        <div className="flex gap-4 justify-end">
          <LinkButton
            variant="outline"
            disabled={isLoading}
            href={ROUTES.WORDS.INDEX}
          >
            Cancel
          </LinkButton>
          <Button type="submit" disabled={isLoading}>
            {submitButtonText}
            {isLoading && <Spinner />}
          </Button>
        </div>
      </CardStack>
    </form>
  )
}

interface AddSynonymFormProps {
  onAddSynonym: (synonym: string) => boolean
}

const AddSynonymForm = ({ onAddSynonym }: AddSynonymFormProps) => {
  const [synonymInput, setSynonymInput] = useState("")

  return (
    <>
      <Input
        value={synonymInput}
        onChange={(e) => setSynonymInput(e.currentTarget.value)}
      />
      <Button
        variant="secondary"
        onClick={() => {
          const synonym = synonymInput.trim()
          if (!synonym) return
          if (onAddSynonym(synonym)) setSynonymInput("")
        }}
      >
        <Plus />
      </Button>
    </>
  )
}

const DescriptionField = memo(
  ({
    className,
    content,
    onChange,
  }: {
    className?: string
    content?: string
    onChange: (text: string, content: string) => void
  }) => {
    return (
      <RichEditor className={className} content={content} onChange={onChange} />
    )
  }
)
DescriptionField.displayName = "DescriptionField"
