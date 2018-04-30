package tech.testra.reportal.service.scenario

import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono
import tech.testra.reportal.domain.entity.TestScenario
import tech.testra.reportal.exception.ProjectNotFoundException
import tech.testra.reportal.exception.TestScenarioAlreadyExistsException
import tech.testra.reportal.exception.TestScenarioNotFoundException
import tech.testra.reportal.extension.flatMapManyWithResumeOnError
import tech.testra.reportal.extension.flatMapWithResumeOnError
import tech.testra.reportal.extension.onDuplicateKeyException
import tech.testra.reportal.extension.orElseGetException
import tech.testra.reportal.extension.toDomain
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
        tps.getProjectById(projectId)
            .flatMapManyWithResumeOnError {
                tsr.findAllByProjectId(it.id)
            }

    override fun getScenarioById(projectId: String, scenarioId: String): Mono<TestScenario> =
        tps.getProjectById(projectId)
            .flatMapWithResumeOnError {
                tsr.findById(scenarioId).orElseGetException(TestScenarioNotFoundException(scenarioId))
            }

    override fun createScenario(
        projectId: String,
        testScenarioModelMono: Mono<TestScenarioModel>
    ): Mono<TestScenario> {
        return tps.getProjectById(projectId)
            .flatMap {
                testScenarioModelMono.flatMap {
                    // Get feature if exists otherwise create one
                    val testScenarioModel = it
                    tgs.getOrAddGroup(it.featureName, projectId)
                        .flatMap {

                            // If scenario name already exists throw an exception
                            val testScenario = TestScenario(projectId = projectId,
                                name = testScenarioModel.name,
                                featureId = it,
                                backgroundSteps = testScenarioModel.backgroundSteps.toDomain(),
                                steps = testScenarioModel.steps.toDomain())
                            saveScenario(testScenario)
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
        return tps.getProjectById(projectId)
            .switchIfEmpty(ProjectNotFoundException(projectId).toMono())
            .flatMap {
                testScenarioModelMono.flatMap {
                    val testScenarioModel = it
                    tsr.findById(scenarioId)
                        .switchIfEmpty(TestScenarioNotFoundException(scenarioId).toMono())
                        .flatMap {
                            // Get feature if exists otherwise create one
                            tgs.getOrAddGroup(testScenarioModel.featureName, projectId)
                                .flatMap {

                                    // If scenario name already exists throw an exception
                                    val testScenario = TestScenario(id = scenarioId,
                                        projectId = projectId,
                                        name = testScenarioModel.name,
                                        featureId = it,
                                        backgroundSteps = testScenarioModel.backgroundSteps.toDomain(),
                                        steps = testScenarioModel.steps.toDomain())

                                    saveScenario(testScenario)
                                }
                        }
                }
            }
    }

    private fun saveScenario(testScenario: TestScenario): Mono<TestScenario> {
        return tsr.save(testScenario.toMono())
            .flatMap { it.toMono() }
            .onDuplicateKeyException { TestScenarioAlreadyExistsException(testScenario.name) }
    }

    override fun deleteScenarioById(id: String): Mono<Boolean> = tsr.deleteById(id)

    override fun getSize(): Long =
        tsr.size().blockOptional()
            .map { it }
            .orElse(-1L)
}