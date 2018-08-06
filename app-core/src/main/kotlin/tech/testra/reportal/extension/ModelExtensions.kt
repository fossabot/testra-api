package tech.testra.reportal.extension

import tech.testra.reportal.domain.entity.TestCase
import tech.testra.reportal.domain.entity.TestGroup
import tech.testra.reportal.domain.entity.TestResult
import tech.testra.reportal.domain.entity.TestScenario
import tech.testra.reportal.domain.valueobjects.Attachment
import tech.testra.reportal.domain.valueobjects.DataTableCell
import tech.testra.reportal.domain.valueobjects.DataTableRow
import tech.testra.reportal.domain.valueobjects.ResultStatus
import tech.testra.reportal.domain.valueobjects.TestStep
import tech.testra.reportal.domain.valueobjects.TestStepResult
import tech.testra.reportal.model.EnrichedTestResultModel
import tech.testra.reportal.model.ResultType
import tech.testra.reportal.model.TestCaseModel
import tech.testra.reportal.model.TestScenarioModel
import tech.testra.reportal.model.Attachment as AttachmentModel
import tech.testra.reportal.model.DataTableCell as DataTableCellModel
import tech.testra.reportal.model.DataTableRow as DataTableRowModel
import tech.testra.reportal.model.ResultStatus as ResultInModel
import tech.testra.reportal.model.TestStep as TestStepModel
import tech.testra.reportal.model.TestStepResult as TestStepResultModel

fun List<TestStepModel>.toTestStepEntity(): List<TestStep> =
    this.map { TestStep(it.index, it.text, it.dataTableRows.toDataTableRowVO()) }

fun List<TestStep>.toTestStepModel(): List<TestStepModel> = this.map { TestStepModel(it.index, it.text) }

fun TestScenario.isSame(testScenario: TestScenario): Boolean {
    return this.backgroundSteps.areTestStepsSame(testScenario.backgroundSteps) &&
        this.steps.areTestStepsSame(testScenario.steps) && this.tags.sorted() == testScenario.tags.sorted()
}

fun TestCase.isSame(testCase: TestCase): Boolean {
    return this.name == testCase.name && this.namespaceId == testCase.namespaceId &&
        this.tags.containsAll(testCase.tags)
}

fun List<TestStep>.areTestStepsSame(testSteps: List<TestStep>): Boolean {
    return when {
        this.size != testSteps.size -> false
        else -> this.none { testStep ->
            testStep.text != testSteps[testStep.index].text ||
                !testStep.dataTableRows.areDataTableRowsSame(testSteps[testStep.index].dataTableRows)
        }
    }
}

fun List<DataTableRow>.areDataTableRowsSame(dataTableRowsFromRepository: List<DataTableRow>): Boolean {
    return when {
        this.size != dataTableRowsFromRepository.size -> false
        else -> this.none { dataTableRow -> !dataTableRow.isSame(getRowByIndex(dataTableRowsFromRepository, dataTableRow.index)) }
    }
}

private fun DataTableRow.isSame(dataTableRow: DataTableRow): Boolean =
    this.cells.none { cell -> cell.value != getCellByIndex(dataTableRow.cells, cell.index).value }

private fun getRowByIndex(dataTableRows: List<DataTableRow>, index: Int) =
    dataTableRows.first { row -> row.index == index }

private fun getCellByIndex(dataTableCells: List<DataTableCell>, index: Int) =
    dataTableCells.first { cell -> cell.index == index }

fun List<DataTableRowModel>.toDataTableRowVO(): List<DataTableRow> =
    this.map { DataTableRow(it.index, it.cells.toDataTableCellVO()) }

fun List<DataTableCellModel>.toDataTableCellVO(): List<DataTableCell> =
    this.map { DataTableCell(it.index, it.value) }

fun List<TestStepResultModel>.toTestStepResultEntity(): List<TestStepResult> =
    this.map { TestStepResult(it.index, ResultStatus.valueOf(it.status.toString()), it.durationInMs, it.error) }

fun List<AttachmentModel>.toAttachmentEntity(): List<Attachment> =
    this.map { Attachment(it.name, it.mimeType, it.base64EncodedByteArray) }

fun List<TestStepResult>.toTestStepResultModel(): List<TestStepResultModel> =
    this.map { TestStepResultModel(it.index, ResultInModel.valueOf(it.status.toString()), it.durationInMs, it.error) }

fun List<Attachment>.toAttachmentModel(): List<AttachmentModel> =
    this.map { AttachmentModel(it.name, it.mimeType, it.base64EncodedByteArray) }

fun TestScenario.toTestScenarioModel(testGroup: TestGroup): TestScenarioModel =
    TestScenarioModel(
        name = this.name,
        featureName = testGroup.name,
        featureDescription = testGroup.description,
        manual = this.manual,
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
        namespace = testGroup.name,
        manual = this.manual,
        tags = this.tags
    )

fun TestResult.toEnrichedTestResult(testScenario: TestScenario, testGroup: TestGroup): EnrichedTestResultModel =
    this.baseEnrichedTestResultModel().copy(scenario = testScenario.toTestScenarioModel(testGroup))

fun TestResult.toEnrichedTestResult(testCase: TestCase, testGroup: TestGroup): EnrichedTestResultModel =
    this.baseEnrichedTestResultModel().copy(testcase = testCase.toTestCaseModel(testGroup))

fun TestResult.baseEnrichedTestResultModel(): EnrichedTestResultModel =
    EnrichedTestResultModel(
        id = this.id,
        targetId = this.targetId,
        groupId = this.groupId,
        resultType = ResultType.valueOf(this.resultType.toString()),
        status = ResultInModel.valueOf(this.status.toString()),
        error = this.error,
        durationInMs = this.durationInMs,
        startTime = this.startTime,
        endTime = this.endTime,
        retryCount = this.retryCount,
        isExpectedToFail = this.expectedToFail,
        attachments = this.attachments.toAttachmentModel(),
        stepResults = this.stepResults.toTestStepResultModel()
    )