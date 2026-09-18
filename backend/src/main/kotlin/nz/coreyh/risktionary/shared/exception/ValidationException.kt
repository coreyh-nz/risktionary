package nz.coreyh.risktionary.shared.exception

import nz.coreyh.risktionary.shared.exception.code.ErrorCode

/**
 * Thrown when one or more fields of a request fail validation.
 *
 * [fieldErrors] maps a field name (or path, e.g. "configuration.wordIds[2]")
 * to a human-readable description of what is wrong with it, letting the
 * client attribute errors to specific inputs instead of parsing a single
 * message string.
 */
open class ValidationException(
    val fieldErrors: Map<String, String>,
) : AppException(
        errorCode = ErrorCode.VALIDATION_FAILED,
        message = fieldErrors.entries.joinToString { (field, error) -> "$field: $error" },
    )
