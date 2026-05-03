import { CreateGameForm } from "@/features/game/components/create-game-form"

const CreateGamePage = () => {
  return (
    <div className="flex w-full flex-col gap-4">
      <div className="flex flex-col gap-2 text-center">
        <h1 className="text-2xl font-bold tracking-tight">Create Game</h1>
        <p className="text-sm text-muted-foreground">
          Select words and configure your game settings
        </p>
      </div>

      <CreateGameForm />
    </div>
  )
}

export default CreateGamePage
