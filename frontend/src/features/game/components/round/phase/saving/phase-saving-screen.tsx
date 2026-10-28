import { Spinner } from "@/components/ui/spinner"

export const PhaseSavingScreen = () => {
  return (
    <div className="flex flex-1 items-center justify-center gap-2 text-muted-foreground">
      <Spinner />
      <span className="text-sm">Saving round...</span>
    </div>
  )
}
