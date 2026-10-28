import {
  Select,
  SelectContent,
  SelectGroup,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select"
import { cn } from "cn"
import type { Control, FieldValues, Path } from "react-hook-form"
import { FormFieldWrapper } from "./form-field"

interface SelectItemOption {
  value: string | number
  label: string
}

interface FormSelectProps<TFieldValues extends FieldValues> {
  control: Control<TFieldValues>
  name: Path<TFieldValues>
  label?: string
  description?: string
  placeholder?: string
  className?: string
  triggerClassName?: string
  orientation?: "vertical" | "horizontal" | "responsive"
  items?: SelectItemOption[]
  children: React.ReactNode
}

export const FormSelect = <TFieldValues extends FieldValues>({
  control,
  name,
  label,
  description,
  placeholder,
  className,
  triggerClassName,
  orientation = "horizontal",
  items,
  children,
}: FormSelectProps<TFieldValues>) => {
  return (
    <FormFieldWrapper
      control={control}
      name={name}
      label={label}
      description={description}
      className={className}
      orientation={orientation}
      render={({ value, onChange, invalid }) => (
        <Select value={value} onValueChange={onChange} items={items}>
          <SelectTrigger
            id={name}
            className={cn(
              "w-28 shrink-0 grow-0",
              invalid && "border-destructive ring-3 ring-destructive/20",
              triggerClassName
            )}
          >
            <SelectValue placeholder={placeholder} />
          </SelectTrigger>
          <SelectContent>
            <SelectGroup>{children}</SelectGroup>
          </SelectContent>
        </Select>
      )}
    />
  )
}
