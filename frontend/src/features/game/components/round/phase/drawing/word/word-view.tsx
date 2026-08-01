import { assertNever } from "@/lib/utils"
import {
  useGameRoundRole,
  useGameRoundState,
} from "../../../../../stores/game-store-selectors"
import { DrawWordView } from "./draw-word-view"
import { GuessWordView } from "./guess-word-view"
import { GuessedWordView } from "./guessed-word-view"

export const WordView = () => {
  const state = useGameRoundState()
  const role = useGameRoundRole()
  if (state?.type !== "IN_PROGRESS") return
  if (!role) return

  const roleType = role.type
  switch (roleType) {
    case "DRAWER":
      return <DrawWordView word={role.word} />
    case "GUESSER":
      return role.correctGuessWord ? (
        <GuessedWordView word={role.correctGuessWord} />
      ) : (
        <GuessWordView hint={role.hint} />
      )
    default:
      assertNever(roleType)
  }
}
