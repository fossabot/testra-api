package tech.testra.reportal.extension

import tech.testra.reportal.domain.entity.TestScenario
import tech.testra.reportal.domain.valueobjects.Attachment
import tech.testra.reportal.domain.valueobjects.TestStep
import tech.testra.reportal.domain.valueobjects.TestStepResult
import tech.testra.reportal.model.Attachment as AttachmentModel
import tech.testra.reportal.model.TestStep as TestStepModel
import tech.testra.reportal.model.TestStepResult as TestStepResultModel

fun List<TestStepModel>.toTestStepDomain(): List<TestStep> = this.map { TestStep(it.index, it.text) }

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
    this.map { TestStepResult(it.index, it.result, it.durationInMs, it.error) }

fun List<AttachmentModel>.toAttachmentDomain(): List<Attachment> =
    this.map { Attachment(it.name, it.mimeType, it.base64EncodedByteArray) }