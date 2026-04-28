import { CreateGameForm } from "@/features/game/components/create-game-form"

const CreateGamePage = () => {
  return (
    <>
      <div className="text-center">
        <h1 className="text-2xl font-bold tracking-tight">Create Game</h1>
        <p className="mt-1 text-sm text-muted-foreground">
          Select words and configure your game settings
        </p>
      </div>

      <CreateGameForm />
    </>
  )
}

export default CreateGamePage
