import { Client } from "@stomp/stompjs"
import {
  createContext,
  PropsWithChildren,
  useCallback,
  useContext,
  useReducer,
  useRef,
} from "react"

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
  connect: (url: string, onConnect: (client: Client) => void) => void
  disconnect: () => void
  connected: boolean
  attempts: number
  error: string | null
}

const WebSocketContext = createContext<WebSocketContextValue | null>(null)

export const WebSocketProvider = ({ children }: PropsWithChildren) => {
  const [state, dispatch] = useReducer(reducer, initialState)
  const clientRef = useRef<Client | null>(null)
  const attemptsRef = useRef(0)

  const connect = useCallback(
    (url: string, onConnect: (client: Client) => void) => {
      if (clientRef.current?.active) return

      attemptsRef.current = 0
      dispatch({ type: "RESET" })

      const stompClient = new Client({
        brokerURL: url,
        reconnectDelay: 1000,

        onConnect: () => {
          dispatch({ type: "CONNECTED" })
          onConnect(stompClient)
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
    },
    []
  )

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
