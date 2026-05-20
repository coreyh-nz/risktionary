import { Button } from "@/components/ui/button"
import {
  Card,
  CardContent,
  CardFooter,
  CardHeader,
  CardTitle,
} from "@/components/ui/card"
import { useGameSocketVolunteerCommands } from "@/features/game/hooks/socket/use-game-socket-volunteer-commands"
import { GameSessionPlayer } from "@/features/game/types/game"
import clsx from "clsx"
import { useEffect, useState } from "react"
import {
  useGamePlayers,
  useGameSession,
  useVolunteers,
} from "../../stores/game-store-selectors"
import { PlayerChip } from "../lobby/player-chip"
import { PlayerGrid } from "../player-grid"

const VolunteerList = ({
  volunteers,
  selectedId,
  onSelect,
}: {
  volunteers: GameSessionPlayer[]
  selectedId: string | null
  onSelect?: (id: string) => void
}) => {
  const session = useGameSession()

  return (
    <PlayerGrid>
      {volunteers.map((volunteer) => {
        const selected = volunteer.id === selectedId
        return onSelect ? (
          <button key={volunteer.id} onClick={() => onSelect(volunteer.id)}>
            <PlayerChip
              player={volunteer}
              isMe={false} // only the host can select and player chip will never be the host
              className={clsx(
                selected
                  ? "border-primary bg-primary/10 hover:border-primary hover:bg-primary/10" // override default player chip hover styles
                  : "hover:border-primary/40 hover:bg-accent/40"
              )}
            />
          </button>
        ) : (
          <PlayerChip
            key={volunteer.id}
            player={volunteer}
            isMe={
              session?.role === "player" && volunteer.id === session.playerId
            }
            className={clsx(selected && "border-primary bg-primary/10")}
          />
        )
      })}
    </PlayerGrid>
  )
}

export const VolunteeredPlayers = () => {
  const [selectedId, setSelectedId] = useState<string | null>(null)
  const { selectDrawer } = useGameSocketVolunteerCommands()
  const session = useGameSession()
  const players = useGamePlayers()
  const volunteerIds = useVolunteers()

  const volunteers = volunteerIds
    .map((id) => players.find((p) => p.id === id))
    .filter((p) => p !== undefined)

  // unselect if the selected player left or unvolunteered
  useEffect(() => {
    if (selectedId && !volunteerIds.includes(selectedId)) {
      // eslint-disable-next-line react-hooks/set-state-in-effect
      setSelectedId(null)
    }
  }, [selectedId, volunteerIds])

  const toggleSelected = (id: string) =>
    setSelectedId((prev) => (prev === id ? null : id))

  const handleSelectConfirm = () => {
    if (!selectedId) return
    selectDrawer(selectedId)
  }

  const isHost = session?.role === "host"
  const selectedName = volunteers.find((v) => v.id === selectedId)?.displayName

  return (
    <Card>
      <CardHeader>
        <CardTitle>
          Volunteers
          {volunteerIds.length > 0 && (
            <span className="ml-2 text-base font-normal text-muted-foreground">
              ({volunteerIds.length})
            </span>
          )}
        </CardTitle>
      </CardHeader>

      <CardContent className="flex flex-col gap-4">
        {volunteers.length === 0 ? (
          <p className="text-sm text-muted-foreground">
            Waiting for players to raise their hands…
          </p>
        ) : (
          <VolunteerList
            volunteers={volunteers}
            selectedId={selectedId}
            onSelect={isHost ? toggleSelected : undefined}
          />
        )}
      </CardContent>

      {isHost && (
        <CardFooter>
          <Button
            disabled={!selectedId}
            className="w-full"
            onClick={handleSelectConfirm}
          >
            {selectedName ? `Pick ${selectedName}` : "Select a player"}
          </Button>
        </CardFooter>
      )}
    </Card>
  )
}
