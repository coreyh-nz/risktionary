"use client"

import { StepIndicator } from "@/components/common/step-indicator"
import { FormCustom } from "@/components/form"
import { Stack } from "@/components/layout/stack"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card"
import { OutlinedCard } from "@/components/ui/card-variants"
import {
  InputGroup,
  InputGroupAddon,
  InputGroupInput,
} from "@/components/ui/input-group"
import { useWords } from "@/features/words/provider/words-provider"
import { WordSummary } from "@/features/words/types/word"
import { cn } from "cn"
import { ChevronDown, ChevronUp, Plus, Search, Shuffle, X } from "lucide-react"
import { useState } from "react"
import type { Control } from "react-hook-form"
import { CreateGameFormValues } from "../../lib/schemas/create-game-schema"

interface ChooseWordsCardProps {
  control: Control<CreateGameFormValues>
}

export const ChooseWordsCard = ({ control }: ChooseWordsCardProps) => {
  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <StepIndicator>1</StepIndicator>
          Choose your words
        </CardTitle>
        <CardDescription>
          Selected words become the rounds in your game.
        </CardDescription>
      </CardHeader>
      <CardContent>
        <FormCustom control={control} name="wordIds">
          {({ value, onChange }) => (
            <ChooseWordsFields value={value ?? []} onChange={onChange} />
          )}
        </FormCustom>
      </CardContent>
    </Card>
  )
}

interface ChooseWordsFieldsProps {
  value: string[]
  onChange: (value: string[]) => void
}

const ChooseWordsFields = ({ value, onChange }: ChooseWordsFieldsProps) => {
  const { words } = useWords()
  const [query, setQuery] = useState("")

  const selectedWords = value
    .map((id) => words?.find((w) => w.id === id))
    .filter((w): w is WordSummary => !!w)

  const availableWords = (words ?? []).filter(
    (w) =>
      !value.includes(w.id) &&
      (query.trim().length === 0 ||
        w.value.toLowerCase().includes(query.trim().toLowerCase()))
  )

  const addWord = (id: string) => {
    onChange([...value, id])
    setQuery("")
  }

  const removeWord = (id: string) => {
    onChange(value.filter((v) => v !== id))
  }

  const moveWord = (index: number, direction: -1 | 1) => {
    const target = index + direction
    if (target < 0 || target >= value.length) return
    const next = [...value]
    ;[next[index], next[target]] = [next[target], next[index]]
    onChange(next)
  }

  const shuffle = () => {
    const next = [...value]
    for (let i = next.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1))
      ;[next[i], next[j]] = [next[j], next[i]]
    }
    onChange(next)
  }

  return (
    <Stack>
      <InputGroup>
        <InputGroupAddon>
          <Search />
        </InputGroupAddon>
        <InputGroupInput
          placeholder="Search risks to add..."
          value={query}
          onChange={(e) => setQuery(e.target.value)}
        />
      </InputGroup>

      {availableWords.length > 0 && (
        <OutlinedCard>
          <CardContent className="flex flex-wrap gap-2">
            {availableWords.map((word) => (
              <Badge
                key={word.id}
                variant="outline"
                role="button"
                onClick={() => addWord(word.id)}
                className="h-7 cursor-pointer px-2.5 text-[0.8rem] gap-1"
              >
                <Plus className="size-3.5" />
                {word.value}
              </Badge>
            ))}
          </CardContent>
        </OutlinedCard>
      )}

      <OutlinedCard>
        <CardContent>
          <Stack className="gap-3">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm font-semibold">Round order</p>
                <p className="text-xs text-muted-foreground">
                  Use the arrows to reorder, or shuffle automatically.
                </p>
              </div>
              <Button
                type="button"
                variant="outline"
                size="sm"
                disabled={selectedWords.length < 2}
                onClick={shuffle}
              >
                <Shuffle />
                Shuffle
              </Button>
            </div>

            <Stack className="gap-2">
              {selectedWords.map((word, index) => (
                <div
                  key={word.id}
                  className="flex items-center gap-3 rounded-lg border border-border bg-card px-3 py-2"
                >
                  <span className="flex h-6 w-6 shrink-0 items-center justify-center rounded-md border border-border text-xs font-medium text-muted-foreground tabular-nums">
                    {index + 1}
                  </span>
                  <span className="flex-1 text-sm font-medium">
                    {word.value}
                  </span>
                  <div className="flex items-center gap-1">
                    <Button
                      type="button"
                      variant="ghost"
                      size="icon-sm"
                      disabled={index === 0}
                      onClick={() => moveWord(index, -1)}
                    >
                      <ChevronUp />
                    </Button>
                    <Button
                      type="button"
                      variant="ghost"
                      size="icon-sm"
                      disabled={index === selectedWords.length - 1}
                      onClick={() => moveWord(index, 1)}
                    >
                      <ChevronDown />
                    </Button>
                    <Button
                      type="button"
                      variant="ghost"
                      size="icon-sm"
                      onClick={() => removeWord(word.id)}
                    >
                      <X />
                    </Button>
                  </div>
                </div>
              ))}

              {selectedWords.length === 0 && (
                <p
                  className={cn(
                    "py-4 text-center text-sm text-muted-foreground"
                  )}
                >
                  No words selected yet. Search above to add some.
                </p>
              )}
            </Stack>
          </Stack>
        </CardContent>
      </OutlinedCard>
    </Stack>
  )
}
