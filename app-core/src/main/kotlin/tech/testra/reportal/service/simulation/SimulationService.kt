package tech.testra.reportal.service.simulation

import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono
import tech.testra.reportal.domain.entity.Simulation
import tech.testra.reportal.domain.valueobjects.ProjectType
import tech.testra.reportal.exception.ProjectNotFoundException
import tech.testra.reportal.exception.ProjectTypeIsNotSimulationException
import tech.testra.reportal.extension.flatMapManyWithResumeOnError
import tech.testra.reportal.extension.flatMapWithResumeOnError
import tech.testra.reportal.model.SimulationModel
import tech.testra.reportal.repository.ISimulationRepository
import tech.testra.reportal.service.interfaces.ISimulationService
import tech.testra.reportal.service.interfaces.ITestExecutionService
import tech.testra.reportal.service.interfaces.ITestProjectService

@Service
class SimulationService(
    private val _simulationRepository: ISimulationRepository,
    private val _testProjectService: ITestProjectService,
    private val _testExecutionService: ITestExecutionService
) : ISimulationService {

    override fun getSimulationById(projectId: String, executionId: String, resultId: String): Mono<Simulation> =
        _testExecutionService.getExecutionById(projectId, executionId)
            .flatMapWithResumeOnError { _simulationRepository.findById(resultId) }

    override fun getSimulationByProjectAndExecutionIds(projectId: String, executionId: String): Flux<Simulation> =
        _testExecutionService.getExecutionById(projectId, executionId)
            .flatMapManyWithResumeOnError { _simulationRepository.findAll(projectId, executionId) }

    override fun createSimulation(projectId: String, executionId: String, simulationModelM: Mono<SimulationModel>): Mono<Simulation> {
        return _testProjectService.getProject(projectId)
            .switchIfEmpty(ProjectNotFoundException(projectId).toMono())
            .flatMap {
                if (it.projectType == ProjectType.SIMULATION) {
                    simulationModelM.flatMap {
                        val simulation = Simulation(projectId = projectId,
                            executionId = executionId,
                            name = it.name,
                            namespace = it.namespace,
                            scenarios = it.scenarios.toEntity())

                        _simulationRepository.save(simulation.toMono())
                    }
                } else {
                    ProjectTypeIsNotSimulationException(it.id).toMono()
                }
            }
    }

    override fun deleteSimulationById(id: String): Mono<Void> = _simulationRepository.deleteById(id)

    override fun count(): Mono<Long> = _simulationRepository.count()
}