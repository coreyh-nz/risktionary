import { Container } from "@/components/layout/container"
import { Badge } from "@/components/ui/badge"
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card"
import { useGetCurrentGameRoundWord } from "@/features/game/hooks/round/phase/word-review/use-get-current-round-word"
import { useGameSession } from "@/features/game/stores/game-store-selectors"
import { WordReviewView } from "@/features/game/types/round/phase/word-review/word"
import { useEffect, useState } from "react"
import { WordDescription } from "./word-description"

export const PhaseWordReviewScreen = () => {
  const game = useGameSession()
  const { getCurrentGameRoundWord, isLoading, error } =
    useGetCurrentGameRoundWord(game?.gameId ?? "")
  const [word, setWord] = useState<WordReviewView | null>(null)

  useEffect(() => {
    if (!game) return

    const fetch = async () => {
      const response = await getCurrentGameRoundWord()
      if (response.ok) {
        setWord(response.data.word)
      }
    }

    fetch()
  }, [game, getCurrentGameRoundWord])

  if (!game) return

  return (
    <Container size="xl">
      {isLoading && <p>Loading word...</p>}
      {error && <p>Failed to load word.</p>}
      {!isLoading && !error && word && (
        <Card>
          <CardHeader>
            <CardTitle className="text-3xl">{word.value}</CardTitle>
            {word.synonyms.length > 0 && (
              <CardDescription className="flex flex-wrap items-center gap-2">
                <span>Synonyms:</span>
                {word.synonyms.map((synonym) => (
                  <Badge key={synonym} variant="secondary">
                    {synonym}
                  </Badge>
                ))}
              </CardDescription>
            )}
          </CardHeader>
          <CardContent>
            <WordDescription content={word.descriptionContent} />
          </CardContent>
        </Card>
      )}
    </Container>
  )
}
