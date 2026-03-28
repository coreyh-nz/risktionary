package nz.coreyh.risktionary.shared.extensions

import java.util.UUID

inline fun <T> String.toOrNull(transform: (String) -> T): T? = runCatching { transform(this) }.getOrNull()

fun String.toUuidOrNull(): UUID? = toOrNull { UUID.fromString(it) }
