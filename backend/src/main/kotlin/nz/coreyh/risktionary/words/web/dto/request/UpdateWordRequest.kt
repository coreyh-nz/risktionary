package nz.coreyh.risktionary.words.web.dto.request

data class UpdateWordRequest(
    val value: String,
    val synonyms: List<String>,
    val descriptionText: String,
    val descriptionContent: String,
)
