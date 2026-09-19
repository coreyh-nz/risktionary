package nz.coreyh.risktionary.shared.web.advice

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import nz.coreyh.risktionary.shared.exception.AppException
import nz.coreyh.risktionary.shared.exception.ValidationException
import nz.coreyh.risktionary.shared.exception.code.ErrorCode
import nz.coreyh.risktionary.shared.web.dto.ApiErrorResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.resource.NoResourceFoundException

private val logger = KotlinLogging.logger {}

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(NoResourceFoundException::class)
    fun handleNoResourceFoundException(e: NoResourceFoundException): ResponseEntity<ApiErrorResponse> = handleErrorCode(ErrorCode.NOT_FOUND)

    @ExceptionHandler(ValidationException::class)
    fun handleValidationException(
        ex: ValidationException,
        request: HttpServletRequest,
    ): ResponseEntity<ApiErrorResponse> {
        logger.debug {
            "ValidationException handled: " +
                "fieldErrors=${ex.fieldErrors}, " +
                "method=${request.method}, " +
                "uri=${request.requestURI}"
        }

        return handleErrorCode(ex.errorCode, ex.message, ex.fieldErrors)
    }

    @ExceptionHandler(AppException::class)
    fun handleAppException(
        ex: AppException,
        request: HttpServletRequest,
    ): ResponseEntity<ApiErrorResponse> {
        logger.debug {
            "AppException handled: " +
                "code=${ex.errorCode.code}, " +
                "message=${ex.message}, " +
                "type=${ex::class.simpleName}, " +
                "method=${request.method}, " +
                "uri=${request.requestURI}"
        }

        return handleErrorCode(ex.errorCode, ex.message)
    }

    @ExceptionHandler(Exception::class)
    fun handleUnknown(
        ex: Exception,
        request: HttpServletRequest,
    ): ResponseEntity<ApiErrorResponse> {
        logger.error(ex) {
            "Unhandled exception: method=${request.method}, uri=${request.requestURI}"
        }

        return handleErrorCode(ErrorCode.INTERNAL_ERROR)
    }

    private fun handleErrorCode(
        errorCode: ErrorCode,
        message: String? = null,
        fieldErrors: Map<String, String>? = null,
    ): ResponseEntity<ApiErrorResponse> =
        ResponseEntity
            .status(errorCode.httpStatus)
            .body(
                ApiErrorResponse(
                    errorCode = errorCode.code,
                    message = message ?: errorCode.defaultMessage,
                    fieldErrors = fieldErrors,
                ),
            )
}
