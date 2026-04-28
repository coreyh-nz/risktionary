package nz.coreyh.risktionary.game.web.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import nz.coreyh.risktionary.auth.domain.model.UserPrincipal
import nz.coreyh.risktionary.game.application.service.GameSessionService
import nz.coreyh.risktionary.game.domain.model.player.GamePlayerIdentity
import nz.coreyh.risktionary.game.web.dto.request.CreateGameRequest
import nz.coreyh.risktionary.game.web.dto.request.JoinGameRequest
import nz.coreyh.risktionary.game.web.dto.response.CreateGameResponse
import nz.coreyh.risktionary.game.web.dto.response.JoinGameResponse
import nz.coreyh.risktionary.game.web.dto.toHostView
import nz.coreyh.risktionary.game.web.dto.toPlayerView
import nz.coreyh.risktionary.game.web.oas.ApiResponseGameNotFound
import nz.coreyh.risktionary.shared.oas.ApiResponseInternalServerError
import nz.coreyh.risktionary.shared.oas.ApiResponseUnauthorized
import nz.coreyh.risktionary.shared.web.support.Routes
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class GameController(
    private val gameSessionService: GameSessionService,
) {
    @PostMapping(Routes.V1.Game.CREATE)
    @Operation(
        summary = "Create a new game",
        description = "Creates a new game session and assigns the authenticated user as the host.",
    )
    @ApiResponseUnauthorized
    @ApiResponseInternalServerError
    @ApiResponseGameNotFound
    @ApiResponse(
        responseCode = "201",
        description = "Successfully created a game",
        content = [Content(schema = Schema(implementation = CreateGameResponse::class))],
    )
    fun postCreate(
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestBody request: CreateGameRequest,
    ): ResponseEntity<CreateGameResponse> {
        val session = gameSessionService.createSession(hostId = principal.userId)
        val response = CreateGameResponse(session.toHostView())
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @PostMapping(Routes.V1.Game.JOIN)
    @Operation(
        summary = "Join a game",
        description =
            "Allows a user to join a game using a code. Guests must provide a display name, " +
                "while authenticated users will join with their account.",
    )
    @ApiResponseInternalServerError
    @ApiResponseGameNotFound
    @ApiResponse(
        responseCode = "400",
        description = "Missing display name",
        content = [Content()],
    )
    @ApiResponse(
        responseCode = "200",
        description = "Successfully joined the game",
        content = [Content(schema = Schema(implementation = CreateGameResponse::class))],
    )
    fun postJoin(
        @AuthenticationPrincipal principal: UserPrincipal?,
        @RequestBody request: JoinGameRequest,
    ): ResponseEntity<JoinGameResponse> {
        val displayName = request.displayName.trim()
        val identity =
            principal?.let {
                GamePlayerIdentity.Authenticated(displayName, it.userId)
            } ?: GamePlayerIdentity.Guest(displayName)

        val ticket = gameSessionService.handleJoinRequest(request.code, identity)
        val session = gameSessionService.getSession(ticket.gameId)
        val sessionPlayer = session.getPlayer(ticket.playerId)
        val response =
            JoinGameResponse(
                session = session.toPlayerView(),
                ticket = ticket.value,
                playerId = ticket.playerId,
                displayName = sessionPlayer.identity.displayName,
            )
        return ResponseEntity.ok(response)
    }
}
