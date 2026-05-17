"use client"

import { useGetWords } from "@/features/words/hooks/use-get-words"
import { WordSummary } from "@/features/words/types/word"
import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useState,
} from "react"
import { toast } from "sonner"

interface WordsContextValue {
  words: WordSummary[] | undefined
  isLoading: boolean
  removeWord: (id: string) => void
}

const WordsContext = createContext<WordsContextValue | null>(null)

export const WordsProvider = ({ children }: { children: React.ReactNode }) => {
  const { getWords } = useGetWords()
  const [words, setWords] = useState<WordSummary[]>()
  const [isLoading, setIsLoading] = useState(false)

  useEffect(() => {
    const load = async () => {
      setIsLoading(true)
      try {
        const response = await getWords()
        if (response.ok) {
          setWords(response.data.words)
        } else {
          toast.error(`Could not get words: ${response.error.message}`)
        }
      } finally {
        setIsLoading(false)
      }
    }
    load()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  const removeWord = useCallback(async (id: string) => {
    setWords((prev) => prev?.filter((w) => w.id !== id))
  }, [])

  return (
    <WordsContext.Provider value={{ words, isLoading, removeWord }}>
      {children}
    </WordsContext.Provider>
  )
}

export const useWords = () => {
  const ctx = useContext(WordsContext)
  if (!ctx) throw new Error("useWordsContext must be used within WordsProvider")
  return ctx
}
