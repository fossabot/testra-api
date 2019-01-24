package tech.testra.reportal.model

import java.math.BigDecimal

const val EMPTY_STRING = ""

data class ProjectModel(
    val name: String,
    val projectType: ProjectType = ProjectType.TEST,
    val description: String
)

data class TestCaseModel(
    val name: String,
    val namespace: String,
    val className: String,
    val manual: Boolean,
    val tags: List<String> = emptyList()
)

data class TestExecutionModel(
    val description: String = EMPTY_STRING,
    val host: String = EMPTY_STRING,
    val parallel: Boolean,
    val endTime: Long? = null,
    val environment: String = EMPTY_STRING,
    val branch: String = EMPTY_STRING,
    val buildRef: String = EMPTY_STRING,
    val tags: List<String> = emptyList()
)

data class TestResultModel(
    val targetId: String,
    val groupId: String,
    val resultType: ResultType,
    val status: ResultStatus,
    val error: String = EMPTY_STRING,
    val durationInMs: Long,
    val startTime: Long,
    val endTime: Long,
    val retryCount: Long = 0,
    val expectedToFail: Boolean = false,
    val attachments: List<Attachment> = emptyList(),
    val stepResults: List<TestStepResult> = emptyList()
)

data class EnrichedTestResultModel(
    val id: String,
    val targetId: String,
    val groupId: String,
    val resultType: ResultType,
    val status: ResultStatus,
    val error: String,
    val durationInMs: Long,
    val startTime: Long,
    val endTime: Long,
    val retryCount: Long,
    val isExpectedToFail: Boolean,
    val attachments: List<Attachment>,
    val stepResults: List<TestStepResult>,
    val scenario: TestScenarioModel? = null,
    val testcase: TestCaseModel? = null
)

data class TestScenarioModel(
    val name: String,
    val featureName: String,
    val featureDescription: String = EMPTY_STRING,
    val namespace: String = EMPTY_STRING,
    val manual: Boolean,
    val tags: List<String> = emptyList(),
    val before: List<TestStep> = emptyList(),
    val after: List<TestStep> = emptyList(),
    val backgroundSteps: List<TestStep> = emptyList(),
    val steps: List<TestStep>,
    val dataRows: List<DataTableRow> = emptyList()
)

data class TestStep(
    val index: Int,
    val text: String,
    val dataTableRows: List<DataTableRow> = emptyList()
)

data class TestStepResult(
    val index: Int,
    val status: ResultStatus,
    val durationInMs: Long = 0,
    val error: String = EMPTY_STRING
)

data class SimulationModel(
    val name: String,
    val namespace: String,
    val scenarios: List<SimulationScenario>
)

data class SecurityScanResult(
    val scanner: SecurityScanner,
    val scannerVersion: String = EMPTY_STRING,
    val alerts: List<VulnerabilityAlert>
)

enum class SecurityScanner {
    ZAP, Nessus
}

data class VulnerabilityAlert(
    val name: String,
    val description: String,
    val riskLevel: VulnerabilityRiskLevel,
    val domain: String,
    val urls: List<UrlResource>,
    val solution: String,
    val otherInfo: String = EMPTY_STRING,
    val reference: String = EMPTY_STRING
)

data class UrlResource(
    val url: String,
    val method: HttpMethod,
    val params: List<String>
)

data class CounterModel(
    val projectsCount: Long = 0,
    val testScenariosCount: Long = 0,
    val testCasesCount: Long = 0,
    val testExecutionsCount: Long = 0,
    val testResultsCount: Long = 0,
    val simulationsCount: Long = 0,
    val vulnerabilityAlertsCount: Long = 0
)

data class ProjectCounterModel(
    val testExecutionsCount: Long = 0,
    val testResultsCount: Long = 0,
    val testScenariosCount: Long = 0,
    val testCasesCount: Long = 0,
    val simulationsCount: Long = 0,
    val vulnerabilityAlertsCount: Long = 0
)

data class ProjectExecutionCounter(
    val projectName: String,
    val noOfExecutions: Long
)

data class Attachment(
    val name: String = EMPTY_STRING,
    val mimeType: String,
    val base64EncodedByteArray: String
)

data class TestExecutionFilters(
    val environments: List<String> = emptyList(),
    val branches: List<String> = emptyList(),
    val tags: List<String> = emptyList()
)

enum class ResultStatus {
    PASSED, FAILED, OTHERS, SKIPPED, PENDING, AMBIGUOUS, UNDEFINED, UNKNOWN
}

enum class ResultType {
    SCENARIO, TEST_CASE
}

data class DataTableCell(
    val index: Int,
    val value: String
)

data class DataTableRow(
    val index: Int,
    val cells: List<DataTableCell>
)

enum class ProjectType {
    TEST, SIMULATION, SECURITY
}

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
    val avgRequestPerSec: BigDecimal
)

data class Percentile(
    val n: Byte,
    val value: BigDecimal
)

enum class VulnerabilityRiskLevel {
    INFO, LOW, MEDIUM, HIGH
}

enum class HttpMethod {
    GET, POST, PUT, DELETE, HEAD, OPTIONS, TRACE, PATCH, CONNECT
}