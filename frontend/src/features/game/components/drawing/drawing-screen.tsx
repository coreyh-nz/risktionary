import { useGameIsDrawer } from "../../stores/game-store-selectors"
import { ChatPanel } from "../round/chat/chat-panel"
import { RoundWordView } from "../round/word/round-word-view"
import { DrawerPanel } from "./panel/drawer-panel"
import { SpectatorPanel } from "./panel/spectator-panel"

export const DrawingScreen = () => {
  const isDrawer = useGameIsDrawer()

  return (
    <div className="flex flex-col flex-1 gap-3 min-h-0">
      <div className="flex justify-center shrink-0">
        <RoundWordView />
      </div>
      <div className="flex gap-3 flex-1 min-w-0 min-h-0">
        <div className="flex flex-1 min-w-0">
          {isDrawer ? <DrawerPanel /> : <SpectatorPanel />}
        </div>

        <ChatPanel className="w-72 self-stretch shrink-0" />
      </div>
    </div>
  )
}
