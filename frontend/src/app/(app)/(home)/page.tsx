import { Card, CardContent } from "@/components/ui/card"
import { Logo } from "@/components/common/logo"
import { JoinGameForm } from "@/features/game/components/join-game-form"
import { AuthSection } from "@/features/auth/components/auth-section"

const HomePage = async () => {
  return (
    <>
      <Logo size="xl" className="justify-center" />

      <Card>
        <CardContent>
          <JoinGameForm />
        </CardContent>
      </Card>

      <AuthSection />
    </>
  )
}

export default HomePage
