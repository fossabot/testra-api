package tech.testra.reportal.domain.valueobjects

data class TestStep(
    val index: Int,
    val text: String
)

data class TestStepResult(
    val index: Int,
    val result: Result,
    val duration: Long,
    val error: String
)