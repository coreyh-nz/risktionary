import { Button } from "@/components/ui/button"
import {
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { useState } from "react"

interface AddImageDialogProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  onInsert: (url: string) => void
}

export const AddImageDialog = ({
  open,
  onOpenChange,
  onInsert,
}: AddImageDialogProps) => {
  const [imageUrl, setImageUrl] = useState("")

  const handleInsert = () => {
    if (imageUrl) {
      onInsert(imageUrl)
      setImageUrl("")
      onOpenChange(false)
    }
  }

  const handleOpenChange = (open: boolean) => {
    if (!open) setImageUrl("")
    onOpenChange(open)
  }

  return (
    <Dialog open={open} onOpenChange={handleOpenChange}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <DialogTitle>Insert Image</DialogTitle>
        </DialogHeader>
        <div className="flex flex-col gap-2 py-2">
          <Label htmlFor="image-url">Image URL</Label>
          <Input
            id="image-url"
            placeholder="https://example.com/image.png"
            value={imageUrl}
            onChange={(e) => setImageUrl(e.target.value)}
            onKeyDown={(e) => e.key === "Enter" && handleInsert()}
            autoFocus
          />
        </div>
        <DialogFooter>
          <Button variant="outline" onClick={() => handleOpenChange(false)}>
            Cancel
          </Button>
          <Button onClick={handleInsert} disabled={!imageUrl}>
            Insert
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
