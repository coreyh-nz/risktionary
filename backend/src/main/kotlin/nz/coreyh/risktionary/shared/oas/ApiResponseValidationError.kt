package nz.coreyh.risktionary.shared.oas

import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@ApiResponse(
    responseCode = "400",
    description = "The request did not pass validation",
    content = [Content()],
)
annotation class ApiResponseValidationError
