import { sharedExtensions } from "@/lib/tiptap/extensions"
import { renderToReactElement } from "@tiptap/static-renderer"
import { useMemo } from "react"

interface WordDescriptionProps {
  content: string
}

export const WordDescription = ({ content }: WordDescriptionProps) => {
  const rendered = useMemo(
    () =>
      renderToReactElement({
        content: JSON.parse(content),
        extensions: sharedExtensions,
      }),
    [content]
  )

  return (
    <div className="tiptap max-w-none max-h-none! overflow-visible!">
      {rendered}
    </div>
  )
}
