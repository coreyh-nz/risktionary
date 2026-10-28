import { useCallback, useEffect, useRef, useState } from "react"
import { ApiError } from "@/lib/api/errors"
import { apiRequest, ApiResponse } from "@/lib/api/request"

interface UseApiOptions<TBody> extends Omit<RequestInit, "body"> {
  body?: TBody
}

interface UseApiReturn<TResult, TBody> {
  request: (options?: UseApiOptions<TBody>) => Promise<ApiResponse<TResult>>
  isLoading: boolean
  error: ApiError | null
  clearError: () => void
}

export const useApi = <TResult, TBody = unknown>(
  url: string,
  defaultOptions?: UseApiOptions<TBody>
): UseApiReturn<TResult, TBody> => {
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState<ApiError | null>(null)
  const optionsRef = useRef(defaultOptions)

  useEffect(() => {
    optionsRef.current = defaultOptions
  }, [defaultOptions])

  const request = useCallback(
    async (callOptions?: UseApiOptions<TBody>) => {
      setIsLoading(true)
      setError(null)

      const merged = { ...optionsRef.current, ...callOptions }
      const res = await apiRequest<TResult, TBody>(url, merged)

      setIsLoading(false)

      if (!res.ok) {
        setError(res.error)
      }

      return res
    },
    [url]
  )

  return {
    request,
    isLoading,
    error,
    clearError: () => setError(null),
  }
}
