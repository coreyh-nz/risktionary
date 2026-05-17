"use client"

import { Button } from "@/components/ui/button"
import { Separator } from "@/components/ui/separator"
import { Toggle } from "@/components/ui/toggle"
import { cn } from "@/lib/utils"
import Blockquote from "@tiptap/extension-blockquote"
import Bold from "@tiptap/extension-bold"
import BulletList from "@tiptap/extension-bullet-list"
import Document from "@tiptap/extension-document"
import Heading from "@tiptap/extension-heading"
import History from "@tiptap/extension-history"
import Image from "@tiptap/extension-image"
import Italic from "@tiptap/extension-italic"
import ListItem from "@tiptap/extension-list-item"
import OrderedList from "@tiptap/extension-ordered-list"
import Paragraph from "@tiptap/extension-paragraph"
import Placeholder from "@tiptap/extension-placeholder"
import Text from "@tiptap/extension-text"
import { Editor, EditorContent, useEditor, useEditorState } from "@tiptap/react"
import {
  Bold as BoldIcon,
  Heading1,
  Heading2,
  Heading3,
  Image as ImageIcon,
  Italic as ItalicIcon,
  List,
  ListOrdered,
  Redo2,
  TextQuote,
  Undo2,
} from "lucide-react"
import { useCallback, useState } from "react"
import { AddImageDialog } from "./add-image-dialog"

interface RichEditorProps {
  className?: string
  content?: string
  placeholder?: string
  onChange?: (text: string, content: string) => void
}

export function RichEditor({
  className,
  content = "",
  placeholder = "Start writing…",
  onChange,
}: RichEditorProps) {
  const editor = useEditor({
    extensions: [
      Document,
      Paragraph,
      Text,
      Bold,
      Italic,
      Heading.configure({ levels: [1, 2, 3] }),
      BulletList,
      OrderedList,
      ListItem,
      Blockquote,
      Image,
      History,
      Placeholder.configure({ placeholder }),
    ],
    content: content ? JSON.parse(content) : "",
    onUpdate({ editor }: { editor: Editor }) {
      onChange?.(editor.getText(), JSON.stringify(editor.getJSON()))
    },
  })
  const {
    isActiveBold,
    isActiveItalic,
    isActiveH1,
    isActiveH2,
    isActiveH3,
    canUndo,
    canRedo,
  } = useEditorState({
    editor,
    selector: (ctx) => {
      return {
        isActiveBold: ctx.editor?.isActive("bold") ?? false,
        isActiveItalic: ctx.editor?.isActive("italic") ?? false,
        isActiveH1: ctx.editor?.isActive("heading", { level: 1 }) ?? false,
        isActiveH2: ctx.editor?.isActive("heading", { level: 2 }) ?? false,
        isActiveH3: ctx.editor?.isActive("heading", { level: 3 }) ?? false,
        canUndo: ctx.editor?.can().chain().focus().undo().run(),
        canRedo: ctx.editor?.can().chain().focus().redo().run(),
      }
    },
  })
  const [imageModalOpen, setImageModalOpen] = useState(false)

  const addImage = useCallback(() => setImageModalOpen(true), [])

  if (!editor) return null

  return (
    <div
      className={cn("rounded-md border border-input bg-background", className)}
    >
      <AddImageDialog
        open={imageModalOpen}
        onOpenChange={setImageModalOpen}
        onInsert={(url) => editor.chain().focus().setImage({ src: url }).run()}
      />
      <div className="flex flex-wrap items-center gap-1 border-b border-input p-1">
        <Toggle
          size="sm"
          pressed={isActiveBold}
          onPressedChange={() => editor.chain().focus().toggleBold().run()}
          aria-label="Bold"
        >
          <BoldIcon className="h-4 w-4" />
        </Toggle>
        <Toggle
          size="sm"
          pressed={isActiveItalic}
          onPressedChange={() => editor.chain().focus().toggleItalic().run()}
          aria-label="Italic"
        >
          <ItalicIcon className="h-4 w-4" />
        </Toggle>

        <Separator orientation="vertical" className="mx-1 h-6" />

        <Toggle
          size="sm"
          pressed={isActiveH1}
          onPressedChange={() =>
            editor.chain().focus().toggleHeading({ level: 1 }).run()
          }
          aria-label="Heading 1"
        >
          <Heading1 className="h-4 w-4" />
        </Toggle>
        <Toggle
          size="sm"
          pressed={isActiveH2}
          onPressedChange={() =>
            editor.chain().focus().toggleHeading({ level: 2 }).run()
          }
          aria-label="Heading 2"
        >
          <Heading2 className="h-4 w-4" />
        </Toggle>
        <Toggle
          size="sm"
          pressed={isActiveH3}
          onPressedChange={() =>
            editor.chain().focus().toggleHeading({ level: 3 }).run()
          }
          aria-label="Heading 3"
        >
          <Heading3 className="h-4 w-4" />
        </Toggle>

        <Separator orientation="vertical" className="mx-1 h-6" />

        <Toggle
          size="sm"
          pressed={editor.isActive("bulletList")}
          onPressedChange={() =>
            editor.chain().focus().toggleBulletList().run()
          }
          aria-label="Bullet list"
        >
          <List className="h-4 w-4" />
        </Toggle>
        <Toggle
          size="sm"
          pressed={editor.isActive("orderedList")}
          onPressedChange={() =>
            editor.chain().focus().toggleOrderedList().run()
          }
          aria-label="Ordered list"
        >
          <ListOrdered className="h-4 w-4" />
        </Toggle>
        <Toggle
          size="sm"
          pressed={editor.isActive("blockquote")}
          onPressedChange={() =>
            editor.chain().focus().toggleBlockquote().run()
          }
          aria-label="Blockquote"
        >
          <TextQuote className="h-4 w-4" />
        </Toggle>
        <Button variant="ghost" size="sm" onClick={addImage} aria-label="Image">
          <ImageIcon className="h-4 w-4" />
        </Button>

        <Separator orientation="vertical" className="mx-1 h-6" />

        <Button
          variant="ghost"
          size="sm"
          onClick={() => editor.chain().focus().undo().run()}
          disabled={!canUndo}
          aria-label="Undo"
        >
          <Undo2 className="h-4 w-4" />
        </Button>
        <Button
          variant="ghost"
          size="sm"
          onClick={() => editor.chain().focus().redo().run()}
          disabled={!canRedo}
          aria-label="Redo"
        >
          <Redo2 className="h-4 w-4" />
        </Button>
      </div>

      <div className="max-w-none">
        <EditorContent editor={editor} className="m-5" />
      </div>
    </div>
  )
}
