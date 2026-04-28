import { Card, CardContent } from "@/components/ui/card"
import { Logo } from "@/components/common/logo"
import { JoinGameForm } from "@/app/(home)/join-game-form"
import { HomeAuthSection } from "@/app/(home)/home-auth-section"

const HomePage = async () => {
  return (
    <>
      <Logo size="xl" className="justify-center" />

      <Card>
        <CardContent>
          <JoinGameForm />
        </CardContent>
      </Card>

      <HomeAuthSection />
    </>
  )
}

export default HomePage
