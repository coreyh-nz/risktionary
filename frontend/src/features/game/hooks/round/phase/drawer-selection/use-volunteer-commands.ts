import { SelectDrawerCommand } from "@/features/game/types/game/commands"
import { useWebSocket } from "@/providers/web-socket-provider"

export const useVolunteerCommands = () => {
  const { send } = useWebSocket()

  const volunteer = () => {
    send("/app/game/volunteer", {})
  }

  const unvolunteer = () => {
    send("/app/game/unvolunteer", {})
  }

  const selectDrawer = (id: string) => {
    const command: SelectDrawerCommand = {
      drawerId: id,
    }
    send("/app/game/select-drawer", command)
  }

  return { volunteer, unvolunteer, selectDrawer }
}
