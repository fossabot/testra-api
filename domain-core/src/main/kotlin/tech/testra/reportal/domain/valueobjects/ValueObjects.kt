package tech.testra.reportal.domain.valueobjects

import java.math.BigDecimal

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

data class SimulationScenario(
    val request: String,
    val startTime: Long,
    val endTime: Long,
    val durationInMs: Long,
    val count: Long,
    val successCount: Long,
    val errorCount: Long,
    val min: BigDecimal,
    val max: BigDecimal,
    val percentiles: List<Percentile>,
    val average: BigDecimal,
    val stdDiv: BigDecimal,
    val avgRequestPerSec: Long
)

data class Percentile(
    val n: Byte,
    val value: Long
)