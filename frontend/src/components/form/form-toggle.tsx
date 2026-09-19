import {
  Field,
  FieldDescription,
  FieldError,
  FieldLabel,
} from "@/components/ui/field"
import { Toggle } from "@/components/ui/toggle"
import type { Control, FieldValues, Path } from "react-hook-form"
import { Controller } from "react-hook-form"

interface FormToggleProps<TFieldValues extends FieldValues> {
  control: Control<TFieldValues>
  name: Path<TFieldValues>
  label?: string
  description?: string
  className?: string
}

export const FormToggle = <TFieldValues extends FieldValues>({
  control,
  name,
  label,
  description,
  className,
}: FormToggleProps<TFieldValues>) => {
  return (
    <Controller
      control={control}
      name={name}
      render={({
        field: { value, onChange, onBlur },
        fieldState: { error },
      }) => (
        <Field
          className={className}
          orientation="horizontal"
          data-invalid={!!error}
        >
          <div>
            {label && <FieldLabel htmlFor={name}>{label}</FieldLabel>}
            {description && !error && (
              <FieldDescription>{description}</FieldDescription>
            )}
            {error?.message && <FieldError>{error.message}</FieldError>}
          </div>
          <Toggle
            id={name}
            pressed={!!value}
            onPressedChange={onChange}
            onBlur={onBlur}
          />
        </Field>
      )}
    />
  )
}
