package nz.coreyh.risktionary.auth.web.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletResponse
import nz.coreyh.risktionary.auth.config.AuthConfiguration
import nz.coreyh.risktionary.shared.oas.ApiResponseInternalServerError
import nz.coreyh.risktionary.shared.oas.ApiResponseUnauthorized
import nz.coreyh.risktionary.shared.web.support.Routes
import nz.coreyh.risktionary.shared.web.support.annotation.Authenticated
import nz.coreyh.risktionary.shared.web.support.deleteCookie
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@Tag(name = "Auth")
class AuthController(
    private val authConfiguration: AuthConfiguration,
) {
    @Authenticated
    @GetMapping(Routes.V1.Auth.LOGOUT)
    @Operation(
        summary = "Logout current user",
        description = "Removes the current access token so it can no longer be used.",
    )
    @ApiResponseUnauthorized
    @ApiResponseInternalServerError
    fun getMe(response: HttpServletResponse) {
        response.deleteCookie(authConfiguration.cookie.accessTokenName) {
            path = authConfiguration.cookie.path
            secure = authConfiguration.cookie.secure
            sameSite = authConfiguration.cookie.sameSite
            httpOnly = true
        }
    }
}
