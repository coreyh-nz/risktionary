"use client"
import { PageSection } from "@/components/layout/page-section"
import { cn } from "@/lib/utils"
import { Hand } from "lucide-react"
import { useGameSocketVolunteerCommands } from "../../../hooks/socket/use-game-socket-volunteer-commands"
import { useGameSession, useVolunteers, } from "../../../stores/game-store-selectors"
import { DrawerSelectionHeader } from "../drawer-selection-header"
import { VolunteeredPlayers } from "../volunteered-players"

export const PlayerDrawerSelectionScreen = () => {
  const { volunteer, unvolunteer } = useGameSocketVolunteerCommands()
  const session = useGameSession()
  const volunteers = useVolunteers()
  const hasVolunteered =
    session?.role === "player" && volunteers.find((p) => session.playerId == p)

  const handleVolunteered = () => {
    if (hasVolunteered) {
      unvolunteer()
    } else {
      volunteer()
    }
  }

  return (
    <PageSection>
      <DrawerSelectionHeader
        title={hasVolunteered ? "You raised your hand!" : "Want to draw?"}
      />

      <div className="flex flex-1 flex-col items-center gap-4">
        <button
          onClick={handleVolunteered}
          className={cn(
            "group relative size-40 rounded-full border-2 transition-all duration-300 active:scale-95",
            hasVolunteered
              ? "border-primary bg-primary/10"
              : "border-border bg-card hover:border-primary/60 hover:bg-primary/5"
          )}
        >
          <div className="relative flex flex-col items-center justify-center gap-1">
            <Hand
              className={cn(
                "size-12 transition-colors duration-300",
                hasVolunteered
                  ? "text-primary"
                  : "text-muted-foreground group-hover:text-primary"
              )}
            />
            <span
              className={cn(
                "text-xs font-semibold tracking-wide transition-colors duration-300",
                hasVolunteered
                  ? "text-primary"
                  : "text-muted-foreground group-hover:text-primary"
              )}
            >
              {hasVolunteered ? "Volunteered!" : "Raise Hand"}
            </span>
          </div>
        </button>
      </div>

      <VolunteeredPlayers />
    </PageSection>
  )
}
