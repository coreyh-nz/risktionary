package nz.coreyh.risktionary.words.web.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import nz.coreyh.risktionary.auth.domain.model.UserPrincipal
import nz.coreyh.risktionary.shared.oas.ApiResponseInternalServerError
import nz.coreyh.risktionary.shared.oas.ApiResponseUnauthorized
import nz.coreyh.risktionary.shared.web.support.Routes
import nz.coreyh.risktionary.words.application.service.WordService
import nz.coreyh.risktionary.words.web.dto.request.CreateWordRequest
import nz.coreyh.risktionary.words.web.dto.request.UpdateWordRequest
import nz.coreyh.risktionary.words.web.dto.response.CreateWordResponse
import nz.coreyh.risktionary.words.web.dto.response.WordResponse
import nz.coreyh.risktionary.words.web.dto.response.WordsResponse
import nz.coreyh.risktionary.words.web.dto.view.toSummaryView
import nz.coreyh.risktionary.words.web.dto.view.toView
import nz.coreyh.risktionary.words.web.oas.ApiResponseWordNotFound
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class WordController(
    private val wordService: WordService,
) {
    @Operation(
        summary = "Get all words",
        description = "Returns a list of all words.",
    )
    @ApiResponseUnauthorized
    @ApiResponseInternalServerError
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Successfully retrieved words",
                content = [Content(schema = Schema(implementation = WordsResponse::class))],
            ),
        ],
    )
    @GetMapping(Routes.V1.Words.BASE)
    fun getWords(): ResponseEntity<WordsResponse> {
        val words = wordService.findWords()
        val response = WordsResponse(words.map { it.toSummaryView() })
        return ResponseEntity.ok(response)
    }

    @Operation(
        summary = "Get a word",
        description = "Returns a single word by its ID.",
    )
    @ApiResponseUnauthorized
    @ApiResponseInternalServerError
    @ApiResponseWordNotFound
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Successfully retrieved word",
                content = [Content(schema = Schema(implementation = WordResponse::class))],
            ),
        ],
    )
    @GetMapping(Routes.V1.Words.INDIVIDUAL)
    fun getWord(
        @PathVariable id: String,
    ): ResponseEntity<WordResponse> {
        val word = wordService.getWord(id)
        val response = WordResponse(word.toView())
        return ResponseEntity.ok(response)
    }

    @Operation(
        summary = "Create a new word",
        description = "Creates a new word and assigns the authenticated user as the creator.",
    )
    @ApiResponseUnauthorized
    @ApiResponseInternalServerError
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "Successfully created word",
                content = [Content(schema = Schema(implementation = CreateWordResponse::class))],
            ),
        ],
    )
    @PostMapping(Routes.V1.Words.BASE)
    fun createWord(
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestBody request: CreateWordRequest,
    ): ResponseEntity<CreateWordResponse> {
        val word =
            wordService.createWord(
                value = request.value.trim(),
                descriptionText = request.descriptionText.trim(),
                descriptionContent = request.descriptionContent.trim(),
                synonyms = request.synonyms.map { it.trim() }.filter { it.isNotBlank() },
                createdBy = principal.userId,
            )
        val response = CreateWordResponse(word)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @Operation(
        summary = "Update an existing word",
        description = "Updates an existing word including its value, description, and synonyms.",
    )
    @ApiResponseUnauthorized
    @ApiResponseInternalServerError
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Successfully updated word",
                content = [Content(schema = Schema(implementation = CreateWordResponse::class))],
            ),
        ],
    )
    @PutMapping(Routes.V1.Words.INDIVIDUAL)
    fun updateWord(
        @PathVariable id: String,
        @RequestBody request: UpdateWordRequest,
    ): ResponseEntity<CreateWordResponse> {
        val word =
            wordService.updateWord(
                id = id,
                value = request.value.trim(),
                descriptionText = request.descriptionText.trim(),
                descriptionContent = request.descriptionContent.trim(),
                synonyms = request.synonyms.map { it.trim() }.filter { it.isNotBlank() },
            )
        val response = CreateWordResponse(word)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @Operation(
        summary = "Delete a word",
        description = "Deletes a word by its ID.",
    )
    @ApiResponseUnauthorized
    @ApiResponseInternalServerError
    @ApiResponseWordNotFound
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "204",
                description = "Successfully deleted word",
                content = [Content()],
            ),
        ],
    )
    @DeleteMapping(Routes.V1.Words.INDIVIDUAL)
    fun deleteWord(
        @PathVariable id: String,
    ): ResponseEntity<Unit> {
        wordService.deleteWord(id)
        return ResponseEntity.noContent().build()
    }
}
