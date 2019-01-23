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

fun List<TestStep>.toTestStepModel(): List<TestStepModel> =
    this.map { TestStepModel(it.index, it.text, it.dataTableRows.toDataTableRowsModel()) }

fun TestScenario.isSame(testScenario: TestScenario): Boolean {
    return this.namespace == testScenario.namespace &&
        this.backgroundSteps.areTestStepsSame(testScenario.backgroundSteps) &&
        this.steps.areTestStepsSame(testScenario.steps) &&
        this.tags.sorted() == testScenario.tags.sorted() &&
        this.dataRows.areDataTableRowsSame(testScenario.dataRows)
}

fun TestCase.isSame(testCase: TestCase): Boolean {
    return this.name == testCase.name && this.namespaceId == testCase.namespaceId &&
        this.tags.containsAll(testCase.tags)
}

fun List<TestStep>.areTestStepsSame(testSteps: List<TestStep>): Boolean {
    return when {
        this.size != testSteps.size -> false
        !this.areTestStepIndexesSame(testSteps) -> false
        else -> this.none { testStep ->
            val testStepFromTarget = getTestStepByIndex(testSteps, testStep.index)
            testStep.text != testStepFromTarget.text ||
                !testStep.dataTableRows.areDataTableRowsSame(testStepFromTarget.dataTableRows)
        }
    }
}

private fun getTestStepByIndex(testSteps: List<TestStep>, index: Int): TestStep =
    testSteps.first { testStep -> testStep.index == index }

private fun List<TestStep>.areTestStepIndexesSame(testSteps: List<TestStep>): Boolean {
    val sourceIndexes = this.map { it.index }
    val targetIndexes = testSteps.map { it.index }
    return sourceIndexes.containsAll(targetIndexes) && targetIndexes.containsAll(sourceIndexes)
}

private fun List<DataTableRow>.areDataTableRowsSame(dataTableRowsFromTarget: List<DataTableRow>): Boolean {
    return when {
        this.size != dataTableRowsFromTarget.size -> false
        !this.areDataTableRowIndexesSame(dataTableRowsFromTarget) -> false
        else -> this.none { dataTableRow -> !dataTableRow.isSame(getRowByIndex(dataTableRowsFromTarget, dataTableRow.index)) }
    }
}

private fun List<DataTableRow>.areDataTableRowIndexesSame(dataTableRowsFromTarget: List<DataTableRow>): Boolean {
    val sourceIndexes = this.map { it.index }
    val targetIndexes = dataTableRowsFromTarget.map { it.index }
    return sourceIndexes.containsAll(targetIndexes) && targetIndexes.containsAll(sourceIndexes)
}

private fun DataTableRow.isSame(dataTableRow: DataTableRow): Boolean =
    when {
        this.cells.size != dataTableRow.cells.size -> false
        else -> this.cells.none { cell -> cell.value != getCellByIndex(dataTableRow.cells, cell.index).value }
    }

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
        namespace = this.namespace,
        manual = this.manual,
        before = this.before.toTestStepModel(),
        after = this.after.toTestStepModel(),
        backgroundSteps = this.backgroundSteps.toTestStepModel(),
        steps = this.steps.toTestStepModel(),
        tags = this.tags,
        dataRows = this.dataRows.toDataTableRowsModel()
    )

private fun List<DataTableRow>.toDataTableRowsModel(): List<DataTableRowModel> =
    this.map { DataTableRowModel(it.index, it.cells.toDataTableCellsModel()) }

private fun List<DataTableCell>.toDataTableCellsModel(): List<DataTableCellModel> =
    this.map { DataTableCellModel(it.index, it.value) }

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