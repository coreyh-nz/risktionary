"use client"

import {
  JoinGameFormValues,
  useJoinGameForm,
} from "@/features/game/hooks/use-join-game-form"
import { FormInput } from "@/components/common/form"
import { Button } from "@/components/ui/button"
import { FieldGroup, FieldSet } from "@/components/ui/field"
import { useJoinGame } from "@/features/game/hooks/use-join-game"
import { ErrorCode } from "@/lib/api/errors"
import { ROUTES } from "@/lib/routes"
import { useRouter } from "next/navigation"
import {
  useGameReset,
  useGameSetPlayer,
  useGameSetState,
} from "@/features/game/stores/game-store-selectors"

export const JoinGameForm = () => {
  const form = useJoinGameForm()
  const { joinGame } = useJoinGame()
  const reset = useGameReset()
  const setState = useGameSetState()
  const setPlayer = useGameSetPlayer()
  const router = useRouter()

  const onSubmit = async (values: JoinGameFormValues) => {
    const response = await joinGame(values)
    if (!response.ok) {
      switch (response.error.errorCode) {
        case ErrorCode.GAME_NOT_FOUND: {
          form.setError("code", {
            type: "manual",
            message: "We couldn't find a game with that code.",
          })
          break
        }
        case ErrorCode.GAME_PLAYER_DISPLAY_NAME_IN_USE: {
          form.setError("displayName", {
            type: "manual",
            message: "There is already a player with that display name.",
          })
          break
        }
      }
      return
    }

    const session = response.data.session
    const ticket = response.data.ticket
    const playerId = response.data.playerId
    const displayName = response.data.displayName

    reset()
    setPlayer(session.id, session.code, ticket, playerId, displayName)
    setState(session.state)
    router.push(ROUTES.GAME.PLAY)
  }

  return (
    <form onSubmit={form.handleSubmit(onSubmit)}>
      <FieldSet>
        <FieldGroup>
          <FormInput control={form.control} name="code" label="Game Code" />
          <FormInput
            control={form.control}
            name="displayName"
            label="Display Name"
          />
        </FieldGroup>

        <Button type="submit" disabled={form.formState.isSubmitting}>
          Join Game
        </Button>
      </FieldSet>
    </form>
  )
}
