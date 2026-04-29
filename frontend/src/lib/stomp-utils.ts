import { IMessage } from "@stomp/stompjs"

export const parseStompMessage = <T>(message: IMessage): T => {
  try {
    return JSON.parse(message.body) as T
  } catch (error) {
    throw new Error("Failed to parse STOMP message body")
  }
}

export const stompHandler =
  <T>(handler: (data: T) => void) =>
  (message: IMessage) => {
    const parsed = JSON.parse(message.body) as T
    handler(parsed)
  }
