import { yupResolver } from "@hookform/resolvers/yup"
import { useForm } from "react-hook-form"
import { array, InferType, object, string } from "yup"

const createWordFormSchema = object({
  word: string().required("Word is required"),
  synonyms: array()
    .of(object({ value: string().required() }))
    .required()
    .default([]),
  descriptionText: string().required("Description is required"),
  descriptionContent: string().required(),
})

export type WordFormValues = InferType<typeof createWordFormSchema>

export const useWordForm = (defaultValues?: WordFormValues) => {
  return useForm<WordFormValues>({
    resolver: yupResolver(createWordFormSchema),
    mode: "onSubmit",
    reValidateMode: "onChange",
    defaultValues: defaultValues || {
      word: "",
      synonyms: [],
      descriptionText: "",
      descriptionContent: "",
    },
  })
}
