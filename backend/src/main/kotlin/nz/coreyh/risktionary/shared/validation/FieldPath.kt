package nz.coreyh.risktionary.shared.validation

/**
 * Builds a dotted field path suitable for [nz.coreyh.risktionary.shared.exception.ValidationException] keys.
 */
fun fieldPath(
    vararg segments: String,
    index: Any? = null,
): String {
    val base = segments.joinToString(".")
    return if (index != null) "$base[$index]" else base
}

/**
 * Convenience for adding an error to a field-errors map.
 */
fun MutableMap<String, String>.addError(
    vararg pathSegments: String,
    index: Any? = null,
    message: String,
) {
    this[fieldPath(*pathSegments, index = index)] = message
}
