package tech.testra.reportal.domain.valueobjects

data class TestStep(
    val index: Int,
    val text: String,
    val dataTableRows: List<DataTableRow>
)

data class TestStepResult(
    val index: Int,
    val status: ResultStatus,
    val durationInMs: Long,
    val error: String
)

data class Attachment(
    val name: String,
    val mimeType: String,
    val base64EncodedByteArray: String
)

data class DataTableCell(
    val index: Int,
    val value: String
)

data class DataTableRow(
    val index: Int,
    val cells: List<DataTableCell>
)