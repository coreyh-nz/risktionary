// Load-test bots for Risktionary.
//
// Each bot joins an existing game over HTTP, connects over STOMP with its
// ticket, and plays like a guesser: it sends chat guesses while the round is in
// the DRAWING phase, rates risks in RANKING, and measures how long AI feedback
// takes to arrive. A summary is printed periodically and on Ctrl+C.
//
// Run with Node 22.18+ (type stripping, no build step):
//   npm run bot -- --code ABC123 --bots 20
//
// Only `import type` is used from src/ so nothing from the Next.js app is
// executed. Keep this file to erasable TS syntax (no enums / parameter props).

import { Client } from "@stomp/stompjs"
import { parseArgs } from "node:util"
import type { GameEvent } from "../../src/features/game/types/game/events.ts"
import type {
  RoundEvent,
  RoundPhaseStateView,
  RoundStateView,
} from "../../src/features/game/types/round/phase/events.ts"
import type { ChatMessage } from "../../src/features/game/types/round/phase/drawing/chat.tsx"

const { values: args } = parseArgs({
  options: {
    code: { type: "string" },
    bots: { type: "string", default: "10" },
    api: { type: "string", default: "http://localhost:8080" },
    ws: { type: "string", default: "ws://localhost:8080/ws" },
    // optional prefix for display names (`<prefix>-Emma`)
    prefix: { type: "string", default: "" },
    // ms between guesses per bot, randomised +-50%
    "guess-interval": { type: "string", default: "8000" },
    // ms between each bot joining
    ramp: { type: "string", default: "200" },
    // comma separated guesses; include the real words to hit correct guesses
    guesses: {
      type: "string",
      default: "phishing,flood,fire,data breach,ransomware,fraud,power outage",
    },
    // the first N bots volunteer to draw
    volunteers: { type: "string", default: "0" },
    // seconds between summaries
    report: { type: "string", default: "15" },
  },
})

if (!args.code) {
  console.error("Usage: npm run bot -- --code <GAME_CODE> [--bots N] ...")
  process.exit(1)
}

const CODE = args.code
const BOTS = Number(args.bots)
const API = args.api!
const WS = args.ws!
const GUESS_INTERVAL = Number(args["guess-interval"])
const RAMP = Number(args.ramp)
const GUESSES = args.guesses!.split(",").map((g) => g.trim())
const VOLUNTEERS = Number(args.volunteers)

const LIKELIHOODS = ["RARE", "UNLIKELY", "POSSIBLE", "LIKELY", "ALMOST_CERTAIN"]
const SEVERITIES = ["INSIGNIFICANT", "MINOR", "MODERATE", "MAJOR", "CATASTROPHIC"]
// 1x1 transparent PNG, enough to exercise the snapshot endpoint
const PNG =
  "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg=="

const NAMES = [
  "Emma", "Liam", "Olivia", "Noah", "Ava", "Oliver", "Sophia", "James",
  "Isabella", "William", "Mia", "Benjamin", "Charlotte", "Lucas", "Amelia",
  "Henry", "Harper", "Alexander", "Evelyn", "Jack", "Abigail", "Daniel",
  "Emily", "Samuel", "Ella", "Joseph", "Grace", "Matthew", "Chloe", "Ethan",
  "Lily", "Leo", "Zoe", "Thomas", "Hannah", "Charlie", "Aria", "George",
  "Ruby", "Max", "Freya", "Oscar", "Sienna", "Jacob", "Isla", "Finn",
  "Nina", "Ryan", "Layla", "Adam",
]

// Names are unique per bot (the backend rejects duplicate display names): a
// shuffled pass through NAMES, then a numbered suffix once they run out.
const shuffledNames = [...NAMES].sort(() => Math.random() - 0.5)
const botName = (index: number) => {
  const name = shuffledNames[index % shuffledNames.length]
  const round = Math.floor(index / shuffledNames.length)
  const unique = round === 0 ? name : `${name}${round + 1}`
  return args.prefix ? `${args.prefix}-${unique}` : unique
}

const pick = <T>(xs: readonly T[]) => xs[Math.floor(Math.random() * xs.length)]
const jitter = (ms: number) => ms * (0.5 + Math.random())
const sleep = (ms: number) => new Promise((r) => setTimeout(r, ms))

interface JoinResponse {
  session: { id: string }
  ticket: string
  playerId: string
  displayName: string
  feedbackEnabled: boolean
}

// ---- stats -----------------------------------------------------------------

const stats = {
  joined: 0,
  joinFailed: 0,
  connectErrors: 0,
  disconnects: 0,
  guessesSent: 0,
  echoes: 0,
  feedbackMessage: 0,
  feedbackRound: 0,
  feedbackDisabledSeen: 0,
  latencies: [] as number[], // ms from own chat echo to FEEDBACK
}

const percentile = (sorted: number[], p: number) =>
  sorted.length ? sorted[Math.min(sorted.length - 1, Math.floor(sorted.length * p))] : 0

const report = (label: string) => {
  const l = [...stats.latencies].sort((a, b) => a - b)
  const pending = stats.echoes - stats.feedbackMessage
  console.log(
    `[${label}] joined=${stats.joined} failed=${stats.joinFailed} ` +
      `connErr=${stats.connectErrors} disc=${stats.disconnects} | ` +
      `guesses=${stats.guessesSent} echoes=${stats.echoes} ` +
      `fb(msg)=${stats.feedbackMessage} fb(round)=${stats.feedbackRound} ` +
      `noFbYet=${Math.max(0, pending)} | latency ms ` +
      `p50=${percentile(l, 0.5)} p95=${percentile(l, 0.95)} ` +
      `max=${l[l.length - 1] ?? 0}`
  )
}

// ---- bot -------------------------------------------------------------------

class Bot {
  name: string
  index: number
  client: Client | null = null
  playerId = ""
  feedbackEnabled = false
  phase = ""
  roundNumber = 0
  isDrawer = false
  guessTimer: ReturnType<typeof setTimeout> | null = null
  snapshotTimer: ReturnType<typeof setInterval> | null = null
  ratedRound = -1
  echoedAt = new Map<string, number>()
  feedbackAt = new Set<string>()

  constructor(index: number) {
    this.index = index
    this.name = botName(index)
  }

  async join() {
    const res = await fetch(`${API}/v1/game/join`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ code: CODE, displayName: this.name }),
    })
    if (!res.ok) {
      stats.joinFailed++
      console.error(`${this.name}: join failed ${res.status} ${await res.text()}`)
      return
    }
    const data = (await res.json()) as JoinResponse
    this.playerId = data.playerId
    this.feedbackEnabled = data.feedbackEnabled
    stats.joined++
    this.connect(data.ticket, data.session.id)
  }

  connect(ticket: string, gameId: string) {
    const client = new Client({
      brokerURL: `${WS}?ticket=${ticket}`,
      reconnectDelay: 1000,
      onConnect: () => {
        client.subscribe(`/topic/game/${gameId}`, (m) =>
          this.onGame(JSON.parse(m.body))
        )
        client.subscribe(`/user/queue/game`, (m) =>
          this.onGame(JSON.parse(m.body))
        )
        client.subscribe("/user/queue/game/round", (m) =>
          this.onRound(JSON.parse(m.body))
        )
        client.subscribe(`/topic/game/${gameId}/round`, (m) =>
          this.onRound(JSON.parse(m.body))
        )
        // tell the server we're subscribed and ready for initial state
        client.publish({
          destination: "/app/player/ready",
          body: JSON.stringify({ gameId }),
        })
      },
      onWebSocketClose: () => {
        stats.disconnects++
      },
      onStompError: (f) => {
        stats.connectErrors++
        console.error(`${this.name}: STOMP error`, f.headers["message"])
      },
    })
    this.client = client
    client.activate()
  }

  send(destination: string, body: unknown = {}) {
    if (!this.client?.active) return
    this.client.publish({
      destination,
      body: JSON.stringify(body),
      headers: { "content-type": "application/json" },
    })
  }

  // ---- events ----

  onGame(event: GameEvent) {
    if (event.type === "STATE" && event.state.type === "IN_PROGRESS") {
      this.roundNumber = event.state.round.number
      this.onRoundState(event.state.round.state)
    }
  }

  onRound(event: RoundEvent) {
    switch (event.type) {
      case "STATE":
        this.onRoundState(event.state)
        break
      case "PHASE_STATE":
        this.onPhase(event.phase)
        break
      case "ASSIGNED_DRAWER":
        this.isDrawer = true
        break
      case "ASSIGNED_GUESSER":
        this.isDrawer = false
        break
      case "CHAT_MESSAGE":
        this.onChat(event.message)
        break
      case "FEEDBACK":
        this.onFeedback(event.feedbackId, event.messageId)
        break
    }
  }

  onRoundState(state: RoundStateView) {
    if (state.type === "SELECTING_DRAWER") {
      this.phase = "SELECTING_DRAWER"
      this.stopPlaying()
      if (this.index < VOLUNTEERS) this.send("/app/game/volunteer", {})
    } else if (state.type === "IN_PROGRESS") {
      this.isDrawer = state.drawer.id === this.playerId
      this.onPhase(state.phase)
    } else {
      this.phase = state.type
      this.stopPlaying()
    }
  }

  onPhase(phase: RoundPhaseStateView) {
    if (phase.type === this.phase) return
    this.phase = phase.type
    this.stopPlaying()

    if (phase.type === "DRAWING") {
      if (this.isDrawer) this.startSnapshots()
      else this.scheduleGuess()
    } else if (phase.type === "RANKING") {
      // one rating per round, after a small random delay
      if (this.ratedRound !== this.roundNumber) {
        this.ratedRound = this.roundNumber
        setTimeout(
          () =>
            this.send("/app/game/risk-rating", {
              likelihood: pick(LIKELIHOODS),
              severity: pick(SEVERITIES),
            }),
          jitter(2000)
        )
      }
    }
  }

  onChat(message: ChatMessage) {
    const mine =
      (message.type === "PLAYER" && message.player.id === this.playerId) ||
      (message.type === "SYSTEM" &&
        message.kind === "PLAYER_GUESSED_CORRECTLY" &&
        message.player.id === this.playerId)
    if (!mine || !("id" in message)) return

    stats.echoes++
    const now = Date.now()
    this.echoedAt.set(message.id, now)
    // feedback beat its own message: measure from now
    if (this.feedbackAt.has(message.id)) {
      this.feedbackAt.delete(message.id)
      stats.latencies.push(0)
    }
  }

  onFeedback(_id: string, messageId: string | null) {
    if (!this.feedbackEnabled) {
      stats.feedbackDisabledSeen++
      return
    }
    if (messageId === null) {
      stats.feedbackRound++
      return
    }
    stats.feedbackMessage++
    const echoed = this.echoedAt.get(messageId)
    if (echoed === undefined) this.feedbackAt.add(messageId)
    else stats.latencies.push(Date.now() - echoed)
  }

  // ---- behaviour ----

  scheduleGuess() {
    this.guessTimer = setTimeout(() => {
      if (this.phase !== "DRAWING" || this.isDrawer) return
      this.send("/app/game/chat", { text: pick(GUESSES) })
      stats.guessesSent++
      this.scheduleGuess()
    }, jitter(GUESS_INTERVAL))
  }

  startSnapshots() {
    this.snapshotTimer = setInterval(
      () => this.send("/app/game/draw/snapshot", { type: "SNAPSHOT", data: PNG }),
      10_000
    )
  }

  stopPlaying() {
    if (this.guessTimer) clearTimeout(this.guessTimer)
    if (this.snapshotTimer) clearInterval(this.snapshotTimer)
    this.guessTimer = null
    this.snapshotTimer = null
  }

  async stop() {
    this.stopPlaying()
    await this.client?.deactivate()
  }
}

// ---- main ------------------------------------------------------------------

const bots = Array.from({ length: BOTS }, (_, i) => new Bot(i))

const reporter = setInterval(
  () => report("stats"),
  Number(args.report) * 1000
)

process.on("SIGINT", async () => {
  clearInterval(reporter)
  console.log("\nStopping bots...")
  await Promise.all(bots.map((b) => b.stop()))
  report("final")
  process.exit(0)
})

console.log(`Joining game ${CODE} with ${BOTS} bots...`)
for (const bot of bots) {
  await bot.join().catch((e) => {
    stats.joinFailed++
    console.error(`${bot.name}:`, e)
  })
  await sleep(RAMP)
}
console.log("All bots joined. Press Ctrl+C to stop.")
