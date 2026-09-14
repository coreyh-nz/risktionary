import { cn } from "cn"
import {
  FormProvider,
  type FieldValues,
  type UseFormReturn,
} from "react-hook-form"

interface FormProps<TFieldValues extends FieldValues> extends Omit<
  React.ComponentProps<"form">,
  "onSubmit"
> {
  form: UseFormReturn<TFieldValues>
  onSubmit?: (values: TFieldValues) => void
}

export const Form = <TFieldValues extends FieldValues>({
  form,
  onSubmit,
  className,
  ...props
}: FormProps<TFieldValues>) => {
  return (
    <FormProvider {...form}>
      <form
        onSubmit={onSubmit ? form.handleSubmit(onSubmit) : undefined}
        className={cn("w-full", className)}
        {...props}
      />
    </FormProvider>
  )
}
