package nz.coreyh.risktionary.game.web.oas

import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@ApiResponse(
    responseCode = "404",
    description = "Drawing analysis was not found, or does not belong to the given game.",
    content = [Content()],
)
annotation class ApiResponseDrawingAnalysisNotFound
