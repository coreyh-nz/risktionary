import type {
  Control,
  ControllerRenderProps,
  FieldValues,
  Path,
} from "react-hook-form"
import { FormFieldWrapper } from "./form-field"

interface FormCustomProps<
  TFieldValues extends FieldValues,
  TName extends Path<TFieldValues> = Path<TFieldValues>,
> {
  control: Control<TFieldValues>
  name: TName
  label?: string
  description?: string
  className?: string
  children: (props: {
    value: ControllerRenderProps<TFieldValues, TName>["value"]
    onChange: ControllerRenderProps<TFieldValues, TName>["onChange"]
    onBlur: ControllerRenderProps<TFieldValues, TName>["onBlur"]
    invalid: boolean
  }) => React.ReactNode
}

function FormCustom<
  TFieldValues extends FieldValues,
  TName extends Path<TFieldValues> = Path<TFieldValues>,
>({
  control,
  name,
  label,
  description,
  className,
  children,
}: FormCustomProps<TFieldValues, TName>) {
  return (
    <FormFieldWrapper
      control={control}
      name={name}
      label={label}
      description={description}
      className={className}
      render={children}
    />
  )
}

export { FormCustom }
