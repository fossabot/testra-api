package tech.testra.reportal.extension

import tech.testra.reportal.domain.entity.TestScenario
import tech.testra.reportal.domain.valueobjects.TestStep
import tech.testra.reportal.model.TestStep as TestStepModel

fun List<TestStepModel>.toDomain(): List<TestStep> = this.map { TestStep(it.index, it.text) }

fun TestScenario.isSame(testScenario: TestScenario) : Boolean {
    return this.backgroundSteps.isSame(testScenario.backgroundSteps) &&
        this.steps.isSame(testScenario.steps)
}

fun List<TestStep>.isSame(testScenarioList: List<TestStep>) : Boolean {
    return when {
        this.isEmpty() -> true
        this.size != testScenarioList.size -> false
        else -> this.filterIndexed { index, testStep -> testStep.text != testScenarioList[index].text }.isEmpty()
    }
}

//fun TestScenarioModel.toEntity(id: String, projectId: String, groupId: String): TestScenario {
//    val testScenario = TestScenario(projectId = projectId,
//        name = this.name,
//        featureId = groupId,
//        backgroundSteps = this.backgroundSteps.toDomain(),
//        steps = this.steps.toDomain())
//}