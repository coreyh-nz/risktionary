import { DrawerPanel } from "@/features/game/components/round/phase/drawing/panel/drawer-panel"
import { SpectatorPanel } from "@/features/game/components/round/phase/drawing/panel/spectator-panel"
import { WordView } from "@/features/game/components/round/phase/drawing/word/word-view"
import {
  useGameIsDrawer,
  useGameIsHost,
} from "../../../../stores/game-store-selectors"
import { ChatPanel } from "./chat/chat-panel"
import { HostPanel } from "./panel/host-panel"

export const PhaseDrawingScreen = () => {
  const isHost = useGameIsHost()
  const isDrawer = useGameIsDrawer()

  const renderPanel = () => {
    if (isHost) return <HostPanel />
    if (isDrawer) return <DrawerPanel />
    return <SpectatorPanel />
  }

  return (
    <div className="flex flex-col flex-1 gap-3 min-h-0">
      <div className="flex justify-center shrink-0">
        <WordView />
      </div>
      <div className="flex gap-3 flex-1 min-w-0 min-h-0">
        <div className="flex flex-1 min-w-0">{renderPanel()}</div>

        <ChatPanel className="w-72 self-stretch shrink-0" />
      </div>
    </div>
  )
}
