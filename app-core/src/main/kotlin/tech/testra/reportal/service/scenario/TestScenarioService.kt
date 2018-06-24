package tech.testra.reportal.service.scenario

import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono
import tech.testra.reportal.domain.entity.TestScenario
import tech.testra.reportal.domain.valueobjects.GroupType
import tech.testra.reportal.exception.ProjectNotFoundException
import tech.testra.reportal.exception.TestScenarioNotFoundException
import tech.testra.reportal.extension.flatMapManyWithResumeOnError
import tech.testra.reportal.extension.flatMapWithResumeOnError
import tech.testra.reportal.extension.isSame
import tech.testra.reportal.extension.orElseGetException
import tech.testra.reportal.extension.toTestStepDomain
import tech.testra.reportal.model.TestScenarioModel
import tech.testra.reportal.repository.ITestScenarioRepository
import tech.testra.reportal.service.interfaces.ITestGroupService
import tech.testra.reportal.service.interfaces.ITestProjectService
import tech.testra.reportal.service.interfaces.ITestScenarioService

@Service
class TestScenarioService(
    val tsr: ITestScenarioRepository,
    val tps: ITestProjectService,
    val tgs: ITestGroupService
) : ITestScenarioService {

    override fun getScenariosByProjectId(projectId: String): Flux<TestScenario> =
        tps.getProject(projectId).flatMapManyWithResumeOnError { tsr.findAllByProjectId(it.id) }

    override fun getScenariosByGroupId(projectId: String, groupId: String): Flux<TestScenario> =
        tps.getProject(projectId)
            .flatMapManyWithResumeOnError {
                tgs.getById(groupId).flatMapManyWithResumeOnError { tsr.findAllByGroupId(it.id) }
            }

    override fun getScenarioById(projectId: String, scenarioId: String): Mono<TestScenario> =
        tps.getProject(projectId)
            .flatMapWithResumeOnError {
                tsr.findById(scenarioId).orElseGetException(TestScenarioNotFoundException(scenarioId))
            }

    override fun createScenario(
        projectId: String,
        testScenarioModelMono: Mono<TestScenarioModel>
    ): Mono<TestScenario> {
        return tps.getProject(projectId)
            .flatMap {
                testScenarioModelMono.flatMap {
                    // Get feature if exists otherwise create one
                    val testScenarioModel = it
                    tgs.getOrAddGroup(
                        groupName = it.featureName,
                        groupDescription = it.featureDescription,
                        type = GroupType.FEATURE,
                        projectId = projectId)
                        .flatMap {

                            // If scenario name already exists throw an exception
                            val testScenario = TestScenario(projectId = projectId,
                                name = testScenarioModel.name,
                                featureId = it,
                                featureDescription = testScenarioModel.featureDescription,
                                tags = testScenarioModel.tags,
                                backgroundSteps = testScenarioModel.backgroundSteps.toTestStepDomain(),
                                steps = testScenarioModel.steps.toTestStepDomain())

                            val testScenarioMono = tsr.findBy(testScenarioModel.name, projectId, it)
                                .filter { ts -> ts.isSame(testScenario) }
                                .next()

                            testScenarioMono
                                .flatMap {
                                    if (it.isSame(testScenario)) it.toMono()
                                    else saveScenario(testScenario)
                                }
                                .switchIfEmpty(saveScenario(testScenario))
                        }
                }
            }
            .onErrorResume { it.toMono() }
    }

    override fun updateScenario(
        projectId: String,
        scenarioId: String,
        testScenarioModelMono: Mono<TestScenarioModel>
    ): Mono<TestScenario> {
        return tps.getProject(projectId)
            .switchIfEmpty(ProjectNotFoundException(projectId).toMono())
            .flatMap {
                testScenarioModelMono.flatMap {
                    val testScenarioModel = it
                    tsr.findById(scenarioId)
                        .switchIfEmpty(TestScenarioNotFoundException(scenarioId).toMono())
                        .flatMap {
                            // Get feature if exists otherwise create one
                            tgs.getOrAddGroup(
                                groupName = testScenarioModel.featureName,
                                groupDescription = it.featureDescription,
                                type = GroupType.FEATURE,
                                projectId = projectId)
                                .flatMap {

                                    // If scenario name already exists throw an exception
                                    val testScenario = TestScenario(id = scenarioId,
                                        projectId = projectId,
                                        name = testScenarioModel.name,
                                        featureId = it,
                                        featureDescription = testScenarioModel.featureDescription,
                                        tags = testScenarioModel.tags,
                                        backgroundSteps = testScenarioModel.backgroundSteps.toTestStepDomain(),
                                        steps = testScenarioModel.steps.toTestStepDomain())

                                    saveScenario(testScenario)
                                }
                        }
                }
            }
    }

    private fun saveScenario(testScenario: TestScenario): Mono<TestScenario> = tsr.save(testScenario.toMono())

    override fun deleteScenarioById(id: String): Mono<Boolean> = tsr.deleteById(id)

    override fun getCount(): Long =
        tsr.size().blockOptional()
            .map { it }
            .orElse(-1L)
}