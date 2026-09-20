import { sharedExtensions } from "@/lib/tiptap/extensions"
import { renderToReactElement } from "@tiptap/static-renderer"
import { useMemo } from "react"

interface WordDescriptionProps {
  content: string
}

export const WordDescription = ({ content }: WordDescriptionProps) => {
  return useMemo(
    () =>
      renderToReactElement({
        content: JSON.parse(content),
        extensions: sharedExtensions,
      }),
    [content]
  )
}
