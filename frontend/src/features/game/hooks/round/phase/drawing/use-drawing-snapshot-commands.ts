import { useWebSocket } from "@/providers/web-socket-provider"
import { useCallback } from "react"

export const useDrawingSnapshotCommands = () => {
  const { send } = useWebSocket()

  const sendSnapshot = useCallback(
    (blob: Blob) => {
      const reader = new FileReader()
      reader.onload = () => {
        send("/app/game/draw/snapshot", {
          type: "SNAPSHOT",
          data: reader.result,
        })
      }
      reader.readAsDataURL(blob)
    },
    [send]
  )

  return { sendSnapshot }
}
