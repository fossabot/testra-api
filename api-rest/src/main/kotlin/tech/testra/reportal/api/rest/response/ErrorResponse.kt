package tech.testra.reportal.api.rest.response

data class ErrorResponse(
    val error: String,
    val msg: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)