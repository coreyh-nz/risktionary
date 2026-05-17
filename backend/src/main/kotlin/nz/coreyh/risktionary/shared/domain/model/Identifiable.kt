package nz.coreyh.risktionary.shared.domain.model

import java.util.UUID

interface Identifiable {
    val value: UUID
}
