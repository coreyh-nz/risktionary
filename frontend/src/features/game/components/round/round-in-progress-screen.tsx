import { CurrentPhase } from "@/features/game/components/round/phase/current-phase"
import { RoundResultModal } from "@/features/game/components/round/round-result-modal"

export const RoundInProgressScreen = () => {
  return (
    <>
      <CurrentPhase />
      <RoundResultModal />
    </>
  )
}
