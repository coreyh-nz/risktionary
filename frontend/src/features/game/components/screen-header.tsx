import { LucideIcon } from "lucide-react"

interface ScreenHeaderProps {
  icon: LucideIcon
  label: string
  title: string
}

export const ScreenHeader = ({
  icon: Icon,
  label,
  title,
}: ScreenHeaderProps) => {
  return (
    <div className="flex items-center justify-center gap-4 py-4">
      <Icon className="size-10 text-primary" />
      <div>
        <p className="text-xs font-semibold uppercase tracking-widest text-muted-foreground">
          {label}
        </p>
        <h2 className="text-2xl font-bold">{title}</h2>
      </div>
    </div>
  )
}
