import { useGameStore } from "@/features/game/stores/game-store"
import { useSyncExternalStore } from "react"

const subscribe = (onChange: () => void) =>
  useGameStore.persist.onFinishHydration(onChange)

// false on the server, true once the persisted session has been read.
// Until then a null session doesn't mean "no session".
export const useGameHydrated = () =>
  useSyncExternalStore(
    subscribe,
    () => useGameStore.persist.hasHydrated(),
    () => false
  )
