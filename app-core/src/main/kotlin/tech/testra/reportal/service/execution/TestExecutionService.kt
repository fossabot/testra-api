package tech.testra.reportal.service.execution

import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono
import tech.testra.reportal.domain.entity.TestExecution
import tech.testra.reportal.exception.TestExecutionNotFoundException
import tech.testra.reportal.extension.flatMapManyWithResumeOnError
import tech.testra.reportal.extension.flatMapWithResumeOnError
import tech.testra.reportal.extension.orElseGetException
import tech.testra.reportal.model.TestExecutionModel
import tech.testra.reportal.repository.ITestExecutionRepository
import tech.testra.reportal.service.interfaces.ITestExecutionService
import tech.testra.reportal.service.interfaces.ITestProjectService

@Service
class TestExecutionService(
    val _testExecutionRepository: ITestExecutionRepository,
    val _projectService: ITestProjectService
) : ITestExecutionService {

    override fun getExecutionsByProjectId(projectId: String): Flux<TestExecution> =
        _projectService.getProject(projectId)
            .flatMapManyWithResumeOnError { _testExecutionRepository.findAll(it.id) }

    override fun getExecutionById(projectId: String, executionId: String): Mono<TestExecution> =
        _projectService.getProject(projectId)
            .flatMapWithResumeOnError {
                _testExecutionRepository.findById(executionId)
                    .orElseGetException(TestExecutionNotFoundException(executionId))
            }

    override fun createExecution(
        projectId: String,
        testExecutionModelMono: Mono<TestExecutionModel>
    ): Mono<TestExecution> {
        return _projectService.getProject(projectId)
            .flatMapWithResumeOnError {
                testExecutionModelMono.flatMap {
                    val testExecution = TestExecution(projectId = projectId,
                        isParallel = it.isParallel,
                        host = it.host,
                        endTime = it.endTime,
                        environment = it.environment,
                        branch = it.branch,
                        tags = it.tags)
                    _testExecutionRepository.save(testExecution.toMono())
                }
            }
    }

    override fun updateExecution(
        projectId: String,
        executionId: String,
        testExecutionModelMono: Mono<TestExecutionModel>
    ): Mono<TestExecution> {
        return _projectService.getProject(projectId)
            .flatMapWithResumeOnError {
                testExecutionModelMono.flatMap {
                    val testExecutionModel = it
                    _testExecutionRepository.findById(executionId)
                        .switchIfEmpty(TestExecutionNotFoundException(executionId).toMono())
                        .flatMap {
                            val testExecution = TestExecution(
                                id = executionId,
                                projectId = projectId,
                                isParallel = testExecutionModel.isParallel,
                                host = testExecutionModel.host,
                                startTime = it.startTime,
                                endTime = testExecutionModel.endTime,
                                environment = testExecutionModel.environment,
                                branch = testExecutionModel.branch,
                                tags = testExecutionModel.tags)
                            _testExecutionRepository.save(testExecution.toMono())
                        }
                }
            }
    }

    override fun updateEndTime(id: String, endTime: Long) {
        _testExecutionRepository.updateEndTime(id, endTime).subscribe()
    }

    override fun deleteExecutionById(id: String): Mono<Boolean> =
        _testExecutionRepository.deleteById(id)

    override fun getSize(): Long =
        _testExecutionRepository.size().blockOptional()
            .map { it }
            .orElse(-1L)
}