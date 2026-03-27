package nz.coreyh.risktionary.shared.oas

import io.swagger.v3.oas.annotations.responses.ApiResponse

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@ApiResponse(
    responseCode = "401",
    description = "User is not authenticated",
)
annotation class ApiResponseUnauthorized
