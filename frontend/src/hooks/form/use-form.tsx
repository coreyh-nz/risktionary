import { yupResolver } from "@hookform/resolvers/yup"
import { useForm as useReactHookForm, type UseFormProps } from "react-hook-form"
import type { AnyObjectSchema, InferType } from "yup"

export const useForm = <TSchema extends AnyObjectSchema>(
  schema: TSchema,
  options?: Omit<UseFormProps<InferType<TSchema>>, "resolver">
) => {
  return useReactHookForm<InferType<TSchema>>({
    resolver: yupResolver(schema),
    ...options,
  })
}
