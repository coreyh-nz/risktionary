import { Pencil } from "lucide-react"
import { ScreenHeader } from "../screen-header"

interface DrawerSelectionHeader {
  title: string
}

export const DrawerSelectionHeader = ({ title }: DrawerSelectionHeader) => {
  return <ScreenHeader icon={Pencil} label="Drawer Selection" title={title} />
}
