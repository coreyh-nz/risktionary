"use client"
import { PageSection } from "@/components/layout/page-section"
import { DrawerSelectionHeader } from "../drawer-selection-header"
import { VolunteeredPlayers } from "../volunteered-players"

export const HostDrawerSelectionScreen = () => {
  return (
    <PageSection>
      <DrawerSelectionHeader title="Who wants to draw?" />
      <VolunteeredPlayers />
    </PageSection>
  )
}
