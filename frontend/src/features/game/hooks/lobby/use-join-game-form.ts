import { useForm } from "react-hook-form"
import { yupResolver } from "@hookform/resolvers/yup"
import { InferType, object, string } from "yup"

const joinGameFormSchema = object({
  code: string()
    .required("Game code is required")
    .length(6, "Game code must be exactly 6 characters"),

  displayName: string()
    .required("Display name is required")
    .min(2, "Display name must be at least 2 characters")
    .max(32, "Display name must be at most 32 characters"),
})

export type JoinGameFormValues = InferType<typeof joinGameFormSchema>

export const useJoinGameForm = () => {
  return useForm<JoinGameFormValues>({
    resolver: yupResolver(joinGameFormSchema),
    mode: "onSubmit",
    reValidateMode: "onChange",
    defaultValues: {
      code: "",
      displayName: "",
    },
  })
}
