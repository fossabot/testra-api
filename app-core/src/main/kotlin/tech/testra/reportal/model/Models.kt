package tech.testra.reportal.model

const val EMPTY_STRING = ""

data class ProjectModel(
    val name: String,
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
    val manual: Boolean,
    val tags: List<String> = emptyList(),
    val before: List<TestStep> = emptyList(),
    val after: List<TestStep> = emptyList(),
    val backgroundSteps: List<TestStep> = emptyList(),
    val steps: List<TestStep>
)

data class TestStep(val index: Int, val text: String)

data class TestStepResult(
    val index: Int,
    val status: ResultStatus,
    val durationInMs: Long = 0,
    val error: String = EMPTY_STRING
)

data class CounterModel(
    val projectsCount: Long,
    val testScenariosCount: Long,
    val testCasesCount: Long,
    val testExecutionsCount: Long,
    val testResultsCount: Long
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
    PASSED, FAILED, SKIPPED, PENDING, AMBIGUOUS, UNDEFINED, UNKNOWN
}

enum class ResultType {
    SCENARIO, TEST_CASE
}