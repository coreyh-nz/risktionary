import {
  Client,
  IMessage,
  ReconnectionTimeMode,
  StompSubscription,
} from "@stomp/stompjs"
import {
  createContext,
  PropsWithChildren,
  useCallback,
  useContext,
  useReducer,
  useRef,
} from "react"

const MAX_RETRIES = 5

// must match the server's negotiated heartbeat; 0 would stop the server
// heartbeating us and let idle sockets be closed by proxies.
const HEARTBEAT_MS = 10000

export interface WebSocketError {
  code: string
  message: string
}

interface WebSocketState {
  connected: boolean
  attempts: number
  error: WebSocketError | null
}

type WebSocketAction =
  | { type: "CONNECTED" }
  | { type: "DISCONNECTED" }
  | { type: "ATTEMPT" }
  | { type: "ERROR"; error: WebSocketError }
  | { type: "RESET" }
  | { type: "CLEAR_ERROR" }

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
      return { ...state, error: action.error }
    case "RESET":
      return { ...initialState, error: state.error }
    case "CLEAR_ERROR":
      return { ...state, error: null }
    default:
      return state
  }
}

interface WebSocketContextValue {
  connect: (url: string, onConnect: (client: Client) => void) => void
  disconnect: () => void
  clearError: () => void
  connected: boolean
  attempts: number
  error: WebSocketError | null

  send: (destination: string, body?: unknown) => void
  subscribe: (
    destination: string,
    handler: (message: IMessage) => void
  ) => StompSubscription | undefined
  unsubscribe: (destination: string) => void
}

const WebSocketContext = createContext<WebSocketContextValue | null>(null)

export const WebSocketProvider = ({ children }: PropsWithChildren) => {
  const [state, dispatch] = useReducer(reducer, initialState)
  const clientRef = useRef<Client | null>(null)
  const attemptsRef = useRef(0)
  const fatalRef = useRef(false)

  const connect = useCallback(
    (url: string, onConnect: (client: Client) => void) => {
      if (clientRef.current?.active) return

      attemptsRef.current = 0
      fatalRef.current = false
      // RESET keeps any prior error so the UI can still react to it;
      // it is cleared on CONNECTED or by the consumer via clearError.
      dispatch({ type: "RESET" })

      const stompClient = new Client({
        brokerURL: url,
        reconnectDelay: 1000,
        reconnectTimeMode: ReconnectionTimeMode.EXPONENTIAL,
        maxReconnectDelay: 10000,
        heartbeatIncoming: HEARTBEAT_MS,
        heartbeatOutgoing: HEARTBEAT_MS,
        splitLargeFrames: true,

        onConnect: () => {
          // a successful connection means earlier failures were transient.
          attemptsRef.current = 0
          dispatch({ type: "CONNECTED" })
          onConnect(stompClient)
        },

        onDisconnect: () => {
          dispatch({ type: "DISCONNECTED" })
        },

        onWebSocketClose: () => {
          dispatch({ type: "DISCONNECTED" })
          if (fatalRef.current) return

          attemptsRef.current += 1
          dispatch({ type: "ATTEMPT" })

          if (attemptsRef.current >= MAX_RETRIES) {
            stompClient.reconnectDelay = 0
            void stompClient.deactivate()
            dispatch({
              type: "ERROR",
              error: {
                code: "client.connection-failed",
                message: `Failed to connect after ${MAX_RETRIES} attempts.`,
              },
            })
          }
        },

        onStompError: (frame) => {
          console.error("STOMP error", frame)
          fatalRef.current = true
          stompClient.reconnectDelay = 0
          void stompClient.deactivate()
          dispatch({
            type: "ERROR",
            error: {
              code: frame.headers["code"] ?? "generic.bad-request",
              message: frame.headers["message"] ?? "Connection refused",
            },
          })
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

  const clearError = useCallback(() => dispatch({ type: "CLEAR_ERROR" }), [])

  const send = useCallback((destination: string, body: unknown = {}) => {
    const client = clientRef.current
    if (!client?.connected) {
      console.warn(`[WebSocket] Cannot send to ${destination} - not connected`)
      return
    }

    client.publish({
      destination,
      body: typeof body === "string" ? body : JSON.stringify(body),
      headers: { "content-type": "application/json" },
    })
  }, [])

  const subscribe = useCallback(
    (destination: string, handler: (message: IMessage) => void) => {
      const client = clientRef.current
      if (!client?.connected) {
        console.warn(
          `[WebSocket] Cannot subscribe to ${destination} - not connected`
        )
        return
      }
      return client.subscribe(destination, handler)
    },
    []
  )

  const unsubscribe = useCallback((destination: string) => {
    const client = clientRef.current
    if (!client?.connected) {
      console.warn(
        `[WebSocket] Cannot unsubscribe to ${destination} - not connected`
      )
      return
    }
    client.unsubscribe(destination)
  }, [])

  return (
    <WebSocketContext.Provider
      value={{
        connect,
        clearError,
        disconnect,
        send,
        connected: state.connected,
        attempts: state.attempts,
        error: state.error,
        subscribe,
        unsubscribe,
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
