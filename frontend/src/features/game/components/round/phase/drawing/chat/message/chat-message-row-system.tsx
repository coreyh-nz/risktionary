import { FeedbackThread } from "@/features/game/components/feedback/feedback-thread"
import { useGameIsPlayerMe } from "@/features/game/stores/game-store-selectors"
import { SystemMessage } from "@/features/game/types/round/phase/drawing/chat"
import { assertNever } from "@/lib/utils"
import { cva, type VariantProps } from "class-variance-authority"
import { cn } from "cn"
import { CheckCircle2, Clock, type LucideIcon, Trophy } from "lucide-react"

const systemMessageRowVariants = cva("flex items-center gap-2 rounded-md", {
  variants: {
    tone: {
      neutral: "text-muted-foreground italic",
      success: "bg-emerald-500/10 text-emerald-700 dark:text-emerald-300",
      warning: "bg-amber-500/10 text-amber-700 dark:text-amber-300",
      info: "bg-blue-500/10 text-blue-700 dark:text-blue-300",
    },
    inset: {
      true: "mx-2 my-1 px-3 py-1.5",
      false: "px-3.5 py-1.5",
    },
  },
  defaultVariants: {
    inset: true,
  },
})

// Same `tone` variant, but scoped to just the icon's own color classes.
// Kept as a separate cva (rather than a compound variant on the row) since
// the icon needs its own className, not the row's.
const systemMessageIconVariants = cva("size-3.5 shrink-0", {
  variants: {
    tone: {
      neutral: "",
      success: "text-emerald-600 dark:text-emerald-400",
      warning: "text-amber-600 dark:text-amber-400",
      info: "text-blue-600 dark:text-blue-400",
    },
  },
  defaultVariants: {
    tone: "neutral",
  },
})

interface SystemMessageRowProps extends VariantProps<
  typeof systemMessageRowVariants
> {
  icon?: LucideIcon
  text: string
}

export const SystemChatMessageRow = ({
  message,
}: {
  message: SystemMessage
}) => {
  const kind = message.kind
  switch (kind) {
    case "DRAWER_SELECTED":
      return <DrawerSelectedRow player={message.player} />
    case "PLAYER_GUESSED_CORRECTLY":
      return (
        <PlayerGuessedCorrectlyRow
          messageId={message.id}
          player={message.player}
        />
      )
    case "DRAWING_ENDED_ALL_GUESSED":
      return (
        <SystemMessageRow
          icon={Trophy}
          text={`Everyone guessed it! The word was "${message.word}"`}
          tone="warning"
        />
      )
    case "DRAWING_ENDED_TIME_UP":
      return (
        <SystemMessageRow
          icon={Clock}
          text={`Time's up! The word was "${message.word}"`}
          tone="info"
        />
      )
    default:
      assertNever(kind)
  }
}

const SystemMessageRow = ({
  icon: Icon,
  text,
  tone,
  inset,
}: SystemMessageRowProps) => (
  <div className={cn(systemMessageRowVariants({ tone, inset }))}>
    {Icon && <Icon className={systemMessageIconVariants({ tone })} />}
    <span className="text-[12px] font-medium">{text}</span>
  </div>
)

const DrawerSelectedRow = ({
  player,
}: {
  player: Extract<SystemMessage, { kind: "DRAWER_SELECTED" }>["player"]
}) => {
  const isMe = useGameIsPlayerMe(player.id)
  return (
    <SystemMessageRow
      text={`${isMe ? "You are" : `${player.displayName} is`} drawing!`}
      tone="neutral"
      inset={false}
    />
  )
}

const PlayerGuessedCorrectlyRow = ({
  messageId,
  player,
}: {
  messageId: string
  player: Extract<SystemMessage, { kind: "DRAWER_SELECTED" }>["player"]
}) => {
  const isMe = useGameIsPlayerMe(player.id)
  return (
    <>
      <SystemMessageRow
        icon={CheckCircle2}
        text={`${isMe ? "You" : player.displayName} guessed correctly!`}
        tone="success"
      />
      {isMe && <FeedbackThread messageId={messageId} />}
    </>
  )
}
