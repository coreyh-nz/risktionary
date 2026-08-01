interface DrawWordViewProps {
  word: string
}

export const DrawWordView = ({ word }: DrawWordViewProps) => {
  return (
    <div className="flex flex-col items-center gap-1">
      <span className="text-xs font-semibold uppercase tracking-widest text-muted-foreground">
        Your word to draw
      </span>
      <span className="flex items-center rounded-lg border border-primary/20 bg-primary/10 px-2 h-9 text-lg font-bold text-primary shadow-sm">
        {word}
      </span>
    </div>
  )
}
