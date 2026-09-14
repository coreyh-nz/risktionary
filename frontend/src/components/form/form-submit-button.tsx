import { Button } from "@/components/ui/button"
import { Loader2 } from "lucide-react"
import { useFormContext } from "react-hook-form"

interface FormSubmitButtonProps extends React.ComponentProps<typeof Button> {
  loadingText?: string
}

export const FormSubmitButton = ({
  children,
  loadingText,
  disabled,
  ...props
}: FormSubmitButtonProps) => {
  const { formState } = useFormContext()
  const isSubmitting = formState.isSubmitting

  return (
    <Button type="submit" disabled={isSubmitting || disabled} {...props}>
      {isSubmitting && <Loader2 className="animate-spin" />}
      {isSubmitting && loadingText ? loadingText : children}
    </Button>
  )
}
