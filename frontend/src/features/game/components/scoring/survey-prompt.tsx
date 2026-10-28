import { Button } from "@/components/ui/button"
import { Card, CardContent } from "@/components/ui/card"
import { useGameIsHost } from "@/features/game/stores/game-store-selectors"
import { config } from "@/lib/config"
import { ClipboardList } from "lucide-react"
import Link from "next/link"

export const SurveyPrompt = () => {
  const isHost = useGameIsHost()

  return (
    <Card>
      <CardContent className="flex flex-col items-center gap-3 text-center">
        <div className="flex size-12 items-center justify-center rounded-full bg-primary/10 text-primary">
          <ClipboardList className="size-6" />
        </div>
        <div className="flex flex-col gap-1">
          <h3 className="text-base font-semibold">
            You&apos;re part of a study
          </h3>
          <p className="text-sm text-muted-foreground">
            Thanks for playing! Please take some time to complete our short
            survey to help with our research.
          </p>
        </div>
        {isHost ? (
          <Link
            href="/survey"
            className="mt-1 text-sm font-medium text-primary underline underline-offset-4 hover:text-primary/80"
          >
            {config.surveyUrl}
          </Link>
        ) : (
          <Button render={<Link href="/survey" />} className="mt-1">
            Take the survey
          </Button>
        )}
      </CardContent>
    </Card>
  )
}
