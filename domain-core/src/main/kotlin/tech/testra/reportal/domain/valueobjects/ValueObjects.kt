package tech.testra.reportal.domain.valueobjects

data class TestStep(
    val index: Int,
    val text: String
)

data class TestStepResult(
    val index: Int,
    val result: Result,
    val durationInMs: Long,
    val error: String
)

data class AttachmentVO(val id: String, val name: String)