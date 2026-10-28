import { API_ROUTES } from "@/lib/api/api-routes"
import { apiRequest } from "@/lib/api/request"
import { cookies } from "next/headers"
import { notFound } from "next/navigation"
import { Word } from "../types/word"

interface GetWordResponse {
  word: Word
}

export const getWord = async (id: string): Promise<Word> => {
  const cookieHeader = (await cookies()).toString()
  const response = await apiRequest<GetWordResponse>(
    API_ROUTES.V1.WORDS.individual(id),
    {
      headers: {
        Cookie: cookieHeader,
      },
    }
  )

  if (!response.ok) notFound()
  return response.data.word
}
