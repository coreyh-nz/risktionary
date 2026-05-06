"use client"

import { Button, buttonVariants } from "@/components/ui/button"
import Link from "next/link"
import { ROUTES } from "@/lib/routes"
import { useCreateGame } from "@/features/game/hooks/use-create-game"
import { toast } from "sonner"
import { Spinner } from "@/components/ui/spinner"
import { useRouter } from "next/navigation"
import {
  useGameReset,
  useGameSetHost,
  useGameSetState,
} from "@/features/game/stores/game-store-selectors"

export const CreateGameForm = () => {
  const { createGame, isLoading } = useCreateGame()
  const reset = useGameReset()
  const setState = useGameSetState()
  const setHost = useGameSetHost()
  const router = useRouter()

  const onSubmit = async () => {
    const response = await createGame({})
    if (!response.ok) {
      toast.error(response.error.message)
      return
    }

    const session = response.data.session
    reset()
    setHost(session.id, session.code)
    setState(session.state)
    router.push(ROUTES.GAME.PLAY)
  }

  return (
    <form
      onSubmit={(e) => {
        // will be replaced with react form hook when settings implemented
        e.preventDefault()
        void onSubmit()
      }}
    >
      <div className="flex justify-center gap-4">
        <Link
          href={ROUTES.HOME}
          className={buttonVariants({ variant: "outline" })}
        >
          Cancel
        </Link>
        <Button type="submit" disabled={isLoading}>
          {isLoading && <Spinner />}
          Create Game
        </Button>
      </div>
    </form>
  )
}
