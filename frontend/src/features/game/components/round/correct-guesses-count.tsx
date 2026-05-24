import {
  useGamePlayers,
  useGameRoundCorrectGuessesCount,
} from "../../stores/game-store-selectors"

export const CorrectGuessesCount = () => {
  const players = useGamePlayers()
  const correctGuessesCount = useGameRoundCorrectGuessesCount()

  return (
    <>
      {/* the host can never be a guesser, so subtracting 1 from player count excludes the drawer */}
      {correctGuessesCount} / {players.length - 1}
    </>
  )
}
