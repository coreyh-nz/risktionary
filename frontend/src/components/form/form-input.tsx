import { Input } from "@/components/ui/input"
import { cn } from "cn"
import type { Control, FieldValues, Path } from "react-hook-form"
import { FormFieldWrapper } from "./form-field"

interface FormInputProps<TFieldValues extends FieldValues> extends Omit<
  React.ComponentProps<typeof Input>,
  "name" | "value" | "onChange" | "onBlur"
> {
  control: Control<TFieldValues>
  name: Path<TFieldValues>
  label?: string
  description?: string
}

export const FormInput = <TFieldValues extends FieldValues>({
  control,
  name,
  label,
  description,
  className,
  ...inputProps
}: FormInputProps<TFieldValues>) => {
  return (
    <FormFieldWrapper
      control={control}
      name={name}
      label={label}
      description={description}
      render={({ value, onChange, onBlur, invalid }) => (
        <Input
          id={name}
          value={value ?? ""}
          onChange={onChange}
          onBlur={onBlur}
          className={cn(
            invalid && "border-destructive ring-3 ring-destructive/20",
            className
          )}
          {...inputProps}
        />
      )}
    />
  )
}
