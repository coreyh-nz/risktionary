import { useCallback, useState } from "react"
import { apiRequest, ApiRequestOptions, ApiResponse } from "@/lib/api/request"
import { ApiError } from "@/lib/api/errors"

interface UseApiReturn<TResult, TBody> {
  request: (
    options?: ApiRequestOptions<TBody>
  ) => Promise<ApiResponse<TResult> | null>
  isLoading: boolean
  error: ApiError | null
  clearError: () => void
}

export const useApi = <TResult, TBody = unknown>(
  url: string,
  defaultOptions?: ApiRequestOptions<TBody>
): UseApiReturn<TResult, TBody> => {
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState<ApiError | null>(null)

  const request = useCallback(
    async (callOptions?: ApiRequestOptions<TBody>) => {
      setIsLoading(true)
      setError(null)

      try {
        const merged = { ...defaultOptions, ...callOptions }
        const response = await apiRequest<TResult, TBody>(url, merged)
        if (!response.ok) setError(response.error)
        return response
      } finally {
        setIsLoading(false)
      }
    },
    [url, defaultOptions]
  )

  return { request, isLoading, error, clearError: () => setError(null) }
}
