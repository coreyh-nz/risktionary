import Blockquote from "@tiptap/extension-blockquote"
import Bold from "@tiptap/extension-bold"
import BulletList from "@tiptap/extension-bullet-list"
import Document from "@tiptap/extension-document"
import Heading from "@tiptap/extension-heading"
import Image from "@tiptap/extension-image"
import Italic from "@tiptap/extension-italic"
import ListItem from "@tiptap/extension-list-item"
import OrderedList from "@tiptap/extension-ordered-list"
import Paragraph from "@tiptap/extension-paragraph"
import Text from "@tiptap/extension-text"

/** Extensions shared by the editor and the read-only renderer. */
export const sharedExtensions = [
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
]
