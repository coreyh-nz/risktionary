"use client"

import {
  JoinGameFormValues,
  useJoinGameForm,
} from "@/hooks/game/use-join-game-form"
import { FormInput } from "@/components/common/form"
import { Button } from "@/components/ui/button"
import { FieldGroup, FieldSet } from "@/components/ui/field"

export const JoinGameForm = () => {
  const form = useJoinGameForm()

  const onSubmit = ({ code, displayName }: JoinGameFormValues) => {
    console.log({ code, displayName })
  }

  return (
    <form onSubmit={form.handleSubmit(onSubmit)}>
      <FieldSet>
        <FieldGroup>
          <FormInput control={form.control} name="code" label="Game Code" />
          <FormInput
            control={form.control}
            name="displayName"
            label="Display Name"
          />
        </FieldGroup>

        <Button type="submit" disabled={form.formState.isSubmitting}>
          Join Game
        </Button>
      </FieldSet>
    </form>
  )
}
