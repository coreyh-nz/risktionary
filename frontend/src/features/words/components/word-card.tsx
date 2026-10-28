import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardTitle } from "@/components/ui/card"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import { MoreHorizontal, Pencil, Trash2 } from "lucide-react"
import Link from "next/link"
import { useState } from "react"
import { WordSummary } from "../types/word"
import { ConfirmDeleteWordDialog } from "./delete-word-confirm-dialog"
import { ROUTES } from "@/lib/routes"

interface WordCardProps {
  word: WordSummary
}

export const WordCard = ({ word }: WordCardProps) => {
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false)

  return (
    <Card className="group">
      <CardContent>
        <div className="flex items-center justify-between">
          <CardTitle>{word.value}</CardTitle>
          <DropdownMenu>
            <DropdownMenuTrigger
              render={
                <Button
                  variant="ghost"
                  size="icon"
                  className="size-8 opacity-0 transition-opacity group-hover:opacity-100"
                >
                  <MoreHorizontal className="size-4" />
                </Button>
              }
            />
            <DropdownMenuContent align="end">
              <DropdownMenuItem
                render={
                  <Link href={ROUTES.WORDS.edit(word.id)}>
                    <Pencil className="mr-2 size-4" />
                    Edit
                  </Link>
                }
              ></DropdownMenuItem>

              <DropdownMenuSeparator />

              <DropdownMenuItem
                variant="destructive"
                onClick={(e) => {
                  e.preventDefault()
                  setDeleteDialogOpen(true)
                }}
              >
                <Trash2 className="mr-2 size-4" />
                Delete
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        </div>

        <ConfirmDeleteWordDialog
          word={word}
          open={deleteDialogOpen}
          onOpenChange={setDeleteDialogOpen}
        />

        {/* Synonyms */}
        {word.synonyms.length > 0 && (
          <div className="mb-3">
            <div className="flex flex-wrap gap-1.5">
              {word.synonyms.map((synonym, index) => (
                <Badge
                  key={index}
                  variant="secondary"
                  className="rounded-md px-2 py-0.5 text-xs font-normal"
                >
                  {synonym}
                </Badge>
              ))}
            </div>
          </div>
        )}

        {/* Description preview */}
        {word.description && (
          <div className="rounded-lg bg-muted/50 p-3">
            <p className="mb-1 text-xs font-medium uppercase tracking-wide text-muted-foreground">
              Description
            </p>
            <div
              className="line-clamp-2 text-sm text-muted-foreground"
              dangerouslySetInnerHTML={{ __html: word.description }}
            />
          </div>
        )}
      </CardContent>
    </Card>
  )
}
