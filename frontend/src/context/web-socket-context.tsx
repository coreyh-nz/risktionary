import { Client } from "@stomp/stompjs"
import {
  createContext,
  PropsWithChildren,
  useCallback,
  useContext,
  useReducer,
  useRef,
} from "react"
import { useGameStore } from "@/stores/game-store"

const MAX_RETRIES = 5

interface WebSocketState {
  connected: boolean
  attempts: number
  error: string | null
}

type WebSocketAction =
  | { type: "CONNECTED" }
  | { type: "DISCONNECTED" }
  | { type: "ATTEMPT" }
  | { type: "ERROR"; message: string }
  | { type: "RESET" }

const initialState: WebSocketState = {
  connected: false,
  attempts: 0,
  error: null,
}

const reducer = (
  state: WebSocketState,
  action: WebSocketAction
): WebSocketState => {
  switch (action.type) {
    case "CONNECTED":
      return { ...state, connected: true, attempts: 0, error: null }
    case "DISCONNECTED":
      return { ...state, connected: false }
    case "ATTEMPT":
      return { ...state, attempts: state.attempts + 1 }
    case "ERROR":
      return { ...state, error: action.message }
    case "RESET":
      return initialState
    default:
      return state
  }
}

interface WebSocketContextValue {
  connect: () => void
  disconnect: () => void
  connected: boolean
  attempts: number
  error: string | null
}

const WebSocketContext = createContext<WebSocketContextValue | null>(null)

export const WebSocketProvider = ({ children }: PropsWithChildren) => {
  const { session } = useGameStore()
  const [state, dispatch] = useReducer(reducer, initialState)
  const clientRef = useRef<Client | null>(null)
  const attemptsRef = useRef(0)

  const connect = useCallback(() => {
    if (!session) return
    if (clientRef.current?.active) return

    attemptsRef.current = 0
    dispatch({ type: "RESET" })

    const url =
      session.role === "player"
        ? `ws://localhost:8080/ws?ticket=${session.ticket}`
        : `ws://localhost:8080/ws`

    const stompClient = new Client({
      brokerURL: url,
      reconnectDelay: 1000,

      onConnect: () => {
        dispatch({ type: "CONNECTED" })

        stompClient.publish({
          destination: "/app/player/ready",
          body: JSON.stringify({ gameId: session.gameId }),
        })
      },

      onDisconnect: () => {
        dispatch({ type: "DISCONNECTED" })
      },

      onWebSocketClose: () => {
        dispatch({ type: "DISCONNECTED" })
        attemptsRef.current += 1
        dispatch({ type: "ATTEMPT" })

        if (attemptsRef.current >= MAX_RETRIES) {
          stompClient.reconnectDelay = 0
          void stompClient.deactivate()
          dispatch({
            type: "ERROR",
            message: `Failed to connect after ${MAX_RETRIES} attempts.`,
          })
        }
      },

      onStompError: (frame) => {
        console.error("STOMP error", frame)
      },
    })

    clientRef.current = stompClient
    stompClient.activate()
  }, [session])

  const disconnect = useCallback(() => {
    clientRef.current?.deactivate()
    clientRef.current = null
    dispatch({ type: "DISCONNECTED" })
  }, [])

  return (
    <WebSocketContext.Provider
      value={{
        connect,
        disconnect,
        connected: state.connected,
        attempts: state.attempts,
        error: state.error,
      }}
    >
      {children}
    </WebSocketContext.Provider>
  )
}

export const useWebSocket = () => {
  const context = useContext(WebSocketContext)
  if (!context)
    throw new Error("useWebSocket must be used within WebSocketProvider")
  return context
}
