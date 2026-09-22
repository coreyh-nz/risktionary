package nz.coreyh.risktionary.shared.oas

import io.swagger.v3.oas.annotations.responses.ApiResponse

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@ApiResponse(
    responseCode = "403",
    description = "User does not have the required role",
)
annotation class ApiResponseForbidden
