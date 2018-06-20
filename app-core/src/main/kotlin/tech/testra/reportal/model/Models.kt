package tech.testra.reportal.model

data class ProjectModel(
    val name: String
)

data class TestCaseModel(
    val name: String,
    val namespace: String
)

data class TestExecutionModel(
    val host: String? = null,
    val isParallel: Boolean,
    val endTime: Long? = null,
    val environment: String,
    val branch: String,
    val tags: List<String> = emptyList()
)

data class TestResultModel(
    val targetId: String,
    val resultType: ResultType,
    val result: Result,
    val error: String = "",
    val durationInMs: Long,
    val startTime: Long,
    val endTime: Long,
    val retryCount: Long = 0,
    val attachments: List<Attachment> = emptyList(),
    val stepResults: List<TestStepResult> = emptyList()
)

data class TestScenarioModel(
    val name: String,
    val featureName: String,
    val featureDescription: String = "",
    val tags: List<String> = emptyList(),
    val before: List<TestStep> = emptyList(),
    val after: List<TestStep> = emptyList(),
    val backgroundSteps: List<TestStep> = emptyList(),
    val steps: List<TestStep>
)

data class TestStep(val index: Int, val text: String)

data class TestStepResult(
    val index: Int,
    val result: tech.testra.reportal.domain.valueobjects.Result,
    val durationInMs: Long = 0,
    val error: String = ""
)

data class CounterModel(
    val projectsSize: Long,
    val testScenariosSize: Long,
    val testCasesSize: Long,
    val testExecutionsSize: Long,
    val testResultsSize: Long
)

data class Attachment(val name: String, val base64EncodedByteArray: String)

enum class Result {
    PASSED, FAILED, SKIPPED, PENDING, AMBIGUOUS, UNDEFINED, UNKNOWN
}

enum class ResultType {
    SCENARIO, TEST_CASE
}