import { Textarea } from "@/components/ui/textarea"
import { cn } from "cn"
import type { Control, FieldValues, Path } from "react-hook-form"
import { FormFieldWrapper } from "./form-field"

interface FormTextareaProps<TFieldValues extends FieldValues> extends Omit<
  React.ComponentProps<typeof Textarea>,
  "name" | "value" | "onChange" | "onBlur"
> {
  control: Control<TFieldValues>
  name: Path<TFieldValues>
  label?: string
  description?: string
}

export const FormTextarea = <TFieldValues extends FieldValues>({
  control,
  name,
  label,
  description,
  className,
  ...textareaProps
}: FormTextareaProps<TFieldValues>) => {
  return (
    <FormFieldWrapper
      control={control}
      name={name}
      label={label}
      description={description}
      render={({ value, onChange, onBlur, invalid }) => (
        <Textarea
          id={name}
          value={value ?? ""}
          onChange={onChange}
          onBlur={onBlur}
          className={cn(
            invalid && "border-destructive ring-3 ring-destructive/20",
            className
          )}
          {...textareaProps}
        />
      )}
    />
  )
}
