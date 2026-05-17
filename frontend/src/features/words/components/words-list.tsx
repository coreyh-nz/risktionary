import { useWords } from "../provider/words-provider"
import { WordCard } from "./word-card"

export const WordsList = () => {
  const { words, isLoading } = useWords()

  if (isLoading) return null

  return (
    <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
      {words?.map((word, idx) => (
        <WordCard key={idx} word={word} />
      ))}
    </div>
  )
}
