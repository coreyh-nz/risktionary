import { WordHint } from "../../../types/round"

interface GuessWordViewProps {
  hint: WordHint
}

export const GuessWordView = ({ hint }: GuessWordViewProps) => {
  return (
    <div className="flex flex-col items-center gap-1">
      <span className="text-xs font-semibold uppercase tracking-widest text-muted-foreground">
        Guess the word
      </span>
      <div className="flex gap-2">
        {hint.characters.map((ch, i) => {
          if (ch.type === "SPACE") {
            return <div key={i} className="w-3" />
          }

          return (
            <div
              key={i}
              className="relative flex size-9 items-center justify-center rounded-lg border border-border bg-card text-lg font-bold uppercase shadow-sm"
            >
              {ch.type === "REVEALED" && ch.character}
              {ch.type === "HIDDEN" && (
                <span className="absolute bottom-1.5 left-1/2 h-0.5 w-4 -translate-x-1/2 rounded-full bg-muted-foreground/40" />
              )}
            </div>
          )
        })}
      </div>
    </div>
  )
}
