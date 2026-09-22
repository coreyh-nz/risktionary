package nz.coreyh.risktionary.game.web.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import nz.coreyh.risktionary.game.application.exception.GameNotFoundException
import nz.coreyh.risktionary.game.application.exception.round.DrawingAnalysisNotFoundException
import nz.coreyh.risktionary.game.application.service.GameDetailsService
import nz.coreyh.risktionary.game.domain.model.details.PersistedGameDetails
import nz.coreyh.risktionary.game.domain.model.details.PersistedGameSummary
import nz.coreyh.risktionary.game.domain.model.toGameIdOrNull
import nz.coreyh.risktionary.game.web.oas.ApiResponseDrawingAnalysisNotFound
import nz.coreyh.risktionary.game.web.oas.ApiResponseGameNotFound
import nz.coreyh.risktionary.shared.oas.ApiResponseForbidden
import nz.coreyh.risktionary.shared.oas.ApiResponseInternalServerError
import nz.coreyh.risktionary.shared.oas.ApiResponseUnauthorized
import nz.coreyh.risktionary.shared.web.support.Routes
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

/**
 * Read-only access to the full persisted record of a game, for research
 * analysis. Restricted to users with the RESEARCHER role.
 */
@RestController
class GameDetailsController(
    private val gameDetailsService: GameDetailsService,
) {
    @GetMapping(Routes.V1.Game.BASE)
    @PreAuthorize("hasAuthority('RESEARCHER')")
    @Operation(
        summary = "List all persisted games",
        description =
            "Returns a summary of every persisted game, newest first: id, code, host, creation time, " +
                "feedback generation mode, end reason and end time. Use the game's id with the details " +
                "endpoint to look up everything recorded about it. Restricted to users with the RESEARCHER role.",
    )
    @ApiResponseUnauthorized
    @ApiResponseForbidden
    @ApiResponseInternalServerError
    @ApiResponse(
        responseCode = "200",
        description = "Successfully retrieved the list of games",
        content = [Content(array = ArraySchema(schema = Schema(implementation = PersistedGameSummary::class)))],
    )
    fun getGames(): ResponseEntity<List<PersistedGameSummary>> = ResponseEntity.ok(gameDetailsService.listGames())

    @GetMapping(Routes.V1.Game.DETAILS)
    @PreAuthorize("hasAuthority('RESEARCHER')")
    @Operation(
        summary = "Get everything persisted about a game",
        description =
            "Returns the game's full persisted record for research analysis: configuration, players, " +
                "and every round with its guesses, chat messages, risk ratings, drawing analyses (image " +
                "bytes excluded - fetch those separately), feedback and AI token usage. " +
                "Restricted to users with the RESEARCHER role.",
    )
    @ApiResponseUnauthorized
    @ApiResponseForbidden
    @ApiResponseInternalServerError
    @ApiResponseGameNotFound
    @ApiResponse(
        responseCode = "200",
        description = "Successfully retrieved the game's persisted record",
        content = [Content(schema = Schema(implementation = PersistedGameDetails::class))],
    )
    fun getDetails(
        @PathVariable gameId: String,
    ): ResponseEntity<PersistedGameDetails> {
        val id = gameId.toGameIdOrNull() ?: throw GameNotFoundException()
        return ResponseEntity.ok(gameDetailsService.getDetails(id))
    }

    @GetMapping(Routes.V1.Game.DRAWING_IMAGE)
    @PreAuthorize("hasAuthority('RESEARCHER')")
    @Operation(
        summary = "Get a drawing analysis screenshot",
        description = "Returns the raw image bytes for one drawing analysis of a game. Restricted to users with the RESEARCHER role.",
    )
    @ApiResponseUnauthorized
    @ApiResponseForbidden
    @ApiResponseInternalServerError
    @ApiResponseGameNotFound
    @ApiResponseDrawingAnalysisNotFound
    @ApiResponse(
        responseCode = "200",
        description = "The image bytes",
    )
    fun getDrawingImage(
        @PathVariable gameId: String,
        @PathVariable analysisId: UUID,
    ): ResponseEntity<ByteArray> {
        val id = gameId.toGameIdOrNull() ?: throw GameNotFoundException()
        val image = gameDetailsService.getDrawingImage(id, analysisId) ?: throw DrawingAnalysisNotFoundException()
        return ResponseEntity
            .ok()
            .header(HttpHeaders.CACHE_CONTROL, "private, max-age=31536000, immutable")
            .contentType(MediaType.parseMediaType(image.mimeType))
            .body(image.bytes)
    }
}
