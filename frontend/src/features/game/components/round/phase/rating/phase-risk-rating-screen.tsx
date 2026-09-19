import { Container } from "@/components/layout/container"
import { RiskRatingResultsScreen } from "@/features/game/components/round/phase/rating/risk-rating-results-screen"
import { RiskRatingVoteScreen } from "@/features/game/components/round/phase/rating/risk-rating-vote-screen"
import { RoundNotActiveError } from "@/features/game/errors/round"
import { useGameIsHost } from "@/features/game/stores/game-store-selectors"
import {
  useGameRound,
  useGameRoundSubmittedRiskRating,
} from "@/features/game/stores/selectors/round.selectors"

export const PhaseRiskRatingScreen = () => {
  const round = useGameRound()
  const isHost = useGameIsHost()
  const roundSubmittedRiskRating = useGameRoundSubmittedRiskRating()
  if (!round) throw new RoundNotActiveError()

  if (
    round?.state.type !== "IN_PROGRESS" ||
    (round.state.phase.type !== "RANKING" &&
      round.state.phase.type !== "RANKING_REVIEW")
  )
    return null

  const isVoting =
    round.state.phase.type === "RANKING" &&
    !isHost &&
    roundSubmittedRiskRating === null

  return isVoting ? (
    <Container size="xl">
      <RiskRatingVoteScreen />{" "}
    </Container>
  ) : (
    <Container size="3xl">
      <RiskRatingResultsScreen />{" "}
    </Container>
  )
}
