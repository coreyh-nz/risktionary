package nz.coreyh.risktionary.shared.web.dto

data class ApiErrorResponse(
    val errorCode: String,
    val message: String,
)
