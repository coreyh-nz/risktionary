import { DrawerPanel } from "@/features/game/components/round/phase/drawing/panel/drawer-panel"
import { SpectatorPanel } from "@/features/game/components/round/phase/drawing/panel/spectator-panel"
import { WordView } from "@/features/game/components/round/phase/drawing/word/word-view"
import { useGameIsDrawer } from "../../../../stores/game-store-selectors"
import { ChatPanel } from "./chat/chat-panel"

export const PhaseDrawingScreen = () => {
  const isDrawer = useGameIsDrawer()

  return (
    <div className="flex flex-col flex-1 gap-3 min-h-0">
      <div className="flex justify-center shrink-0">
        <WordView />
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
