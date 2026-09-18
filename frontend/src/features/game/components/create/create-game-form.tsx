"use client"

import { PageHeader, PageTitle } from "@/components/common/typography"
import { Form } from "@/components/form"
import { Stack } from "@/components/layout/stack"
import { WordsProvider } from "@/features/words/provider/words-provider"
import { useForm } from "@/hooks/form/use-form"
import { ROUTES } from "@/lib/routes"
import { useRouter } from "next/navigation"
import { toast } from "sonner"
import { useCreateGame } from "../../hooks/lobby/use-create-game"
import {
  createGameDefaultValues,
  CreateGameFormValues,
  createGameSchema,
} from "../../lib/schemas/create-game-schema"
import {
  useGameReset,
  useGameSetHost,
  useGameSetState,
} from "../../stores/game-store-selectors"
import { GameConfigurationDto } from "../../types/lobby/api"
import { ChooseWordsCard } from "./choose-words-card"
import { FeedbackCard } from "./feedback-card"
import { PaceCard } from "./pace-card"
import { SummaryCard } from "./summary-card"

const toConfiguration = (
  values: CreateGameFormValues
): GameConfigurationDto => ({
  wordIds: values.wordIds,
  lobbyCountdownMs: values.lobbyCountdown,
  skippingCountdownsEnabled: values.skippingCountdownsEnabled,
  phaseDurationsMs: Object.fromEntries(
    Object.entries(values.timers).filter(([, value]) => value !== "none")
  ),
  feedback:
    values.feedbackType === "AI"
      ? {
          type: "AI",
          snapshotIntervalMs: values.aiSnapshotIntervalMs,
          allocation:
            values.feedbackAllocationMode === "FIXED"
              ? {
                  mode: "FIXED",
                  timing: values.feedbackTiming,
                  framing: values.feedbackFraming,
                }
              : { mode: "BALANCED_RANDOM" },
        }
      : { type: "NONE" },
})

export const CreateGameForm = () => {
  const router = useRouter()
  const form = useForm(createGameSchema, {
    defaultValues: createGameDefaultValues,
  })

  const { createGame } = useCreateGame()
  const resetGame = useGameReset()
  const setGameState = useGameSetState()
  const setHost = useGameSetHost()

  const handleSubmit = async (values: CreateGameFormValues) => {
    const response = await createGame({
      configuration: toConfiguration(values),
    })
    if (!response.ok) {
      toast.error(response.error.message)
      return
    }

    const session = response.data.session
    resetGame()
    setHost(session.id, session.code)
    setGameState(session.state)
    router.push(ROUTES.GAME.PLAY)
  }

  return (
    <Form
      form={form}
      onSubmit={handleSubmit}
      className="flex flex-col md:flex-row gap-4"
    >
      <div className="flex flex-col md:flex-row gap-4 w-full">
        <div className="md:basis-8/12">
          <Stack>
            <PageHeader>
              <PageTitle>Create Game</PageTitle>
            </PageHeader>

            <WordsProvider>
              <ChooseWordsCard control={form.control} />
            </WordsProvider>
            <PaceCard control={form.control} />
            <FeedbackCard control={form.control} />
          </Stack>
        </div>

        <div className="md:basis-4/12">
          <SummaryCard control={form.control} />
        </div>
      </div>
    </Form>
  )
}
