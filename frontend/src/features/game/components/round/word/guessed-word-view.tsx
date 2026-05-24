interface GuessedWordViewProps {
  word: string
}

export const GuessedWordView = ({ word }: GuessedWordViewProps) => {
  return (
    <div className="flex flex-col items-center gap-1">
      <span className="text-xs font-semibold uppercase tracking-widest text-muted-foreground">
        You guessed it!
      </span>
      <span className="flex items-center rounded-lg border border-emerald-500/20 bg-emerald-500/10 px-2 h-9 text-lg font-bold text-emerald-600 dark:text-emerald-400 shadow-sm">
        {word}
      </span>
    </div>
  )
}
