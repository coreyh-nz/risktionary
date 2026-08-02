import {
  RoundNotActiveError,
  RoundNotInProgress,
} from "@/features/game/errors/round"
import { useGameRound } from "@/features/game/stores/selectors/round.selectors"
import { assertNever } from "@/lib/utils"
import { useGameRoundRole } from "../../../../../stores/game-store-selectors"
import { DrawWordView } from "./draw-word-view"
import { GuessWordView } from "./guess-word-view"
import { GuessedWordView } from "./guessed-word-view"

export const WordView = () => {
  const round = useGameRound()
  const role = useGameRoundRole()

  if (!round) throw new RoundNotActiveError()
  if (round.state.type !== "IN_PROGRESS") throw new RoundNotInProgress()
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
