"use client"

import { Button } from "@/components/ui/button"

export const RetryButton = () => {
  return (
    <Button variant="outline" onClick={() => window.location.reload()}>
      Try again
    </Button>
  )
}
