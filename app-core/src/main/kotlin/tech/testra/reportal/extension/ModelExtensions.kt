package tech.testra.reportal.extension

import tech.testra.reportal.domain.entity.TestCase
import tech.testra.reportal.domain.entity.TestGroup
import tech.testra.reportal.domain.entity.TestResult
import tech.testra.reportal.domain.entity.TestScenario
import tech.testra.reportal.domain.valueobjects.Attachment
import tech.testra.reportal.domain.valueobjects.Result
import tech.testra.reportal.domain.valueobjects.TestStep
import tech.testra.reportal.domain.valueobjects.TestStepResult
import tech.testra.reportal.model.EnrichedTestResultModel
import tech.testra.reportal.model.ResultType
import tech.testra.reportal.model.TestCaseModel
import tech.testra.reportal.model.TestScenarioModel
import tech.testra.reportal.model.Attachment as AttachmentModel
import tech.testra.reportal.model.Result as ResultInModel
import tech.testra.reportal.model.TestStep as TestStepModel
import tech.testra.reportal.model.TestStepResult as TestStepResultModel

fun List<TestStepModel>.toTestStepDomain(): List<TestStep> = this.map { TestStep(it.index, it.text) }

fun List<TestStep>.toTestStepModel(): List<TestStepModel> = this.map { TestStepModel(it.index, it.text) }

fun TestScenario.isSame(testScenario: TestScenario): Boolean {
    return this.backgroundSteps.isSame(testScenario.backgroundSteps) &&
        this.steps.isSame(testScenario.steps) && this.tags.sorted() == testScenario.tags.sorted()
}

fun List<TestStep>.isSame(testScenarioList: List<TestStep>): Boolean {
    return when {
        this.isEmpty() -> true
        this.size != testScenarioList.size -> false
        else -> this.filterIndexed { index, testStep -> testStep.text != testScenarioList[index].text }.isEmpty()
    }
}

fun List<TestStepResultModel>.toTestStepResultDomain(): List<TestStepResult> =
    this.map { TestStepResult(it.index, Result.valueOf(it.result.toString()), it.durationInMs, it.error) }

fun List<AttachmentModel>.toAttachmentDomain(): List<Attachment> =
    this.map { Attachment(it.name, it.mimeType, it.base64EncodedByteArray) }

fun List<TestStepResult>.toTestStepResultModel(): List<TestStepResultModel> =
    this.map { TestStepResultModel(it.index, ResultInModel.valueOf(it.result.toString()), it.durationInMs, it.error) }

fun List<Attachment>.toAttachmentModel(): List<AttachmentModel> =
    this.map { AttachmentModel(it.name, it.mimeType, it.base64EncodedByteArray) }

fun TestScenario.toTestScenarioModel(testGroup: TestGroup): TestScenarioModel =
    TestScenarioModel(
        name = this.name,
        featureName = testGroup.name,
        featureDescription = testGroup.description,
        before = this.before.toTestStepModel(),
        after = this.after.toTestStepModel(),
        backgroundSteps = this.backgroundSteps.toTestStepModel(),
        steps = this.steps.toTestStepModel(),
        tags = this.tags
    )

fun TestCase.toTestCaseModel(testGroup: TestGroup): TestCaseModel =
    TestCaseModel(
        name = this.name,
        className = testGroup.subGroup,
        namespace = testGroup.name
    )

fun TestResult.toEnrichedTestResult(testScenario: TestScenario, testGroup: TestGroup): EnrichedTestResultModel =
    this.baseEnrichedTestResultModel().copy(scenario = testScenario.toTestScenarioModel(testGroup))

fun TestResult.toEnrichedTestResult(testCase: TestCase, testGroup: TestGroup): EnrichedTestResultModel =
    this.baseEnrichedTestResultModel().copy(testcase = testCase.toTestCaseModel(testGroup))

fun TestResult.baseEnrichedTestResultModel(): EnrichedTestResultModel =
    EnrichedTestResultModel(
        targetId = this.targetId,
        groupId = this.groupId,
        resultType = ResultType.valueOf(this.resultType.toString()),
        result = ResultInModel.valueOf(this.result.toString()),
        error = this.error,
        durationInMs = this.durationInMs,
        startTime = this.startTime,
        endTime = this.endTime,
        retryCount = this.retryCount,
        attachments = this.attachments.toAttachmentModel(),
        stepResults = this.stepResults.toTestStepResultModel()
    )