package nz.coreyh.risktionary.user.web.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import nz.coreyh.risktionary.auth.domain.model.UserPrincipal
import nz.coreyh.risktionary.shared.exception.InvalidUserException
import nz.coreyh.risktionary.shared.oas.ApiResponseInternalServerError
import nz.coreyh.risktionary.shared.oas.ApiResponseUnauthorized
import nz.coreyh.risktionary.shared.web.support.Routes
import nz.coreyh.risktionary.shared.web.support.annotation.Authenticated
import nz.coreyh.risktionary.user.domain.service.UserService
import nz.coreyh.risktionary.user.web.dto.UserDetailsDto
import nz.coreyh.risktionary.user.web.dto.toDto
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@Tag(name = "User")
class UserController(
    private val userService: UserService,
) {
    @Authenticated
    @GetMapping(Routes.V1.User.ME)
    @Operation(
        summary = "Get current user",
        description = "Provides details for the authenticated user.",
    )
    @ApiResponseUnauthorized
    @ApiResponseInternalServerError
    fun getMe(
        @AuthenticationPrincipal principal: UserPrincipal,
    ): UserDetailsDto {
        val user =
            userService.findById(principal.userId)
                ?: throw InvalidUserException()
        return user.toDto()
    }
}
