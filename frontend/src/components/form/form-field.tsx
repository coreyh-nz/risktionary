import {
  Field,
  FieldDescription,
  FieldError,
  FieldLabel,
} from "@/components/ui/field"
import {
  Controller,
  type Control,
  type ControllerRenderProps,
  type FieldValues,
  type Path,
} from "react-hook-form"

interface FormFieldWrapperProps<
  TFieldValues extends FieldValues,
  TName extends Path<TFieldValues> = Path<TFieldValues>,
> {
  control: Control<TFieldValues>
  name: TName
  label?: string
  description?: string
  className?: string
  orientation?: "vertical" | "horizontal" | "responsive"
  render: (props: {
    value: ControllerRenderProps<TFieldValues, TName>["value"]
    onChange: ControllerRenderProps<TFieldValues, TName>["onChange"]
    onBlur: ControllerRenderProps<TFieldValues, TName>["onBlur"]
    invalid: boolean
  }) => React.ReactNode
}

export const FormFieldWrapper = <
  TFieldValues extends FieldValues,
  TName extends Path<TFieldValues> = Path<TFieldValues>,
>({
  control,
  name,
  label,
  description,
  className,
  orientation,
  render,
}: FormFieldWrapperProps<TFieldValues, TName>) => {
  return (
    <Controller
      control={control}
      name={name}
      render={({
        field: { value, onChange, onBlur },
        fieldState: { error },
      }) => (
        <Field
          orientation={orientation}
          className={className}
          data-invalid={!!error}
        >
          {(label || description) && (
            <div className="flex flex-1 flex-col gap-2">
              {label && <FieldLabel htmlFor={name}>{label}</FieldLabel>}
              {description && !error && (
                <FieldDescription>{description}</FieldDescription>
              )}
            </div>
          )}
          {render({ value, onChange, onBlur, invalid: !!error })}
          {error?.message && <FieldError>{error.message}</FieldError>}
        </Field>
      )}
    />
  )
}
