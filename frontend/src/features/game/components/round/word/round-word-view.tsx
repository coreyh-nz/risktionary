import { assertNever } from "@/lib/utils"
import {
  useGameRoundRole,
  useGameRoundState,
} from "../../../stores/game-store-selectors"
import { DrawWordView } from "./draw-word-view"
import { GuessWordView } from "./guess-word-view"

export const RoundWordView = () => {
  const state = useGameRoundState()
  const role = useGameRoundRole()
  if (state?.type !== "IN_PROGRESS") return
  if (!role) return

  const roleType = role.type
  switch (roleType) {
    case "DRAWER":
      return <DrawWordView word={role.word} />
    case "GUESSER":
      return <GuessWordView hint={role.hint} />
    default:
      assertNever(roleType)
  }
}
