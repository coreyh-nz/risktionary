import { ConfirmDialog } from "@/components/common/confirm-dialog"
import { PropsWithChildren } from "react"
import { toast } from "sonner"
import { useDeleteWord } from "../hooks/use-delete-word"
import { useWords } from "../provider/words-provider"
import { WordSummary } from "../types/word"

interface ConfirmDeleteWordDialogProps extends PropsWithChildren {
  word: WordSummary
  open: boolean
  onOpenChange: (open: boolean) => void
}

export const ConfirmDeleteWordDialog = ({
  word,
  open,
  onOpenChange,
}: ConfirmDeleteWordDialogProps) => {
  const { deleteWord, isLoading } = useDeleteWord(word.id)
  const { removeWord } = useWords()

  const handleDelete = async () => {
    const response = await deleteWord()
    if (response.ok) {
      removeWord(word.id)
      toast("Successfully deleted word")
    } else {
      toast.error(`Could not delete word: ${response.error.message}`)
    }
  }

  return (
    <ConfirmDialog
      isLoading={isLoading}
      open={open}
      onOpenChange={onOpenChange}
      title="Delete word?"
      description={`Are you sure you want to delete "${word.value}"?`}
      confirmLabel="Delete"
      confirmVariant="destructive"
      onConfirm={handleDelete}
    />
  )
}
