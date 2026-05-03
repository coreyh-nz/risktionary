import { ApiError, ErrorCode } from "@/lib/api/errors"
import { config } from "@/lib/config"
import { logger } from "@/lib/logger"

export interface ApiRequestOptions<TBody> extends Omit<RequestInit, "body"> {
  body?: TBody
}

export type ApiResponse<T> =
  | { ok: true; data: T }
  | { ok: false; error: ApiError }

export async function apiRequest<TResult, TBody = unknown>(
  url: string,
  options?: ApiRequestOptions<TBody>
): Promise<ApiResponse<TResult>> {
  const { body, ...rest } = options ?? {}
  const fullUrl = `${config.apiUrl}${url}`
  const method = rest.method ?? "GET"

  try {
    const res = await fetch(fullUrl, {
      ...rest,
      ...(body !== undefined && { body: JSON.stringify(body) }),
      headers: {
        "Content-Type": "application/json",
        ...(rest.headers ?? {}),
      },
    })

    const json = await res.json().catch(() => {
      logger.warn(
        { url: fullUrl, method },
        "Failed to parse JSON response"
      )
      return null
    })

    if (!res.ok) {
      const error = new ApiError(
        json?.errorCode ?? ErrorCode.INTERNAL_ERROR,
        json?.message ?? "Unexpected error",
        res.status
      )

      logger.warn(
        {
          url: fullUrl,
          method,
          status: res.status,
          errorCode: error.errorCode,
        },
        "API request failed"
      )

      return {
        ok: false,
        error,
      }
    }

    return {
      ok: true,
      data: json as TResult,
    }
  } catch (err) {
    logger.error(
      {
        url: fullUrl,
        method,
        err: err instanceof Error ? err.message : String(err),
      },
      "API request network or unexpected failure"
    )

    return {
      ok: false,
      error: new ApiError(
        ErrorCode.INTERNAL_ERROR,
        "Unexpected error",
        503
      ),
    }
  }
}