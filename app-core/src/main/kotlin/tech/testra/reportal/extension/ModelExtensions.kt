package tech.testra.reportal.extension

import tech.testra.reportal.domain.valueobjects.TestStep
import tech.testra.reportal.model.TestStep as TestStepModel

fun List<TestStepModel>.toDomain(): List<TestStep> = this.map { TestStep(it.index, it.text) }

//fun TestScenarioModel.toEntity(id: String, projectId: String, groupId: String): TestScenario {
//    val testScenario = TestScenario(projectId = projectId,
//        name = this.name,
//        featureId = groupId,
//        backgroundSteps = this.backgroundSteps.toDomain(),
//        steps = this.steps.toDomain())
//}