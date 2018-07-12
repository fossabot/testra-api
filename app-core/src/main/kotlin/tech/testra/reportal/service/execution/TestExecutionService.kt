package tech.testra.reportal.service.execution

import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.toFlux
import reactor.core.publisher.toMono
import tech.testra.reportal.domain.entity.TestExecution
import tech.testra.reportal.domain.entity.TestExecutionStats
import tech.testra.reportal.exception.ProjectNotFoundException
import tech.testra.reportal.exception.TestExecutionNotFoundException
import tech.testra.reportal.extension.flatMapManyWithResumeOnError
import tech.testra.reportal.extension.flatMapWithResumeOnError
import tech.testra.reportal.extension.orElseGetException
import tech.testra.reportal.model.TestExecutionFilters
import tech.testra.reportal.model.TestExecutionModel
import tech.testra.reportal.repository.ITestExecutionRepository
import tech.testra.reportal.repository.ITestExecutionStatsRepository
import tech.testra.reportal.service.interfaces.ITestExecutionService
import tech.testra.reportal.service.interfaces.ITestProjectService

@Service
class TestExecutionService(
    private val _testExecutionRepository: ITestExecutionRepository,
    private val _testExecutionStatsRepository: ITestExecutionStatsRepository,
    private val _projectService: ITestProjectService
) : ITestExecutionService {

    override fun getExecutions(projectId: String, env: String, branch: String, tags: List<String>) =
        _projectService.getProject(projectId)
            .flatMapManyWithResumeOnError { _testExecutionRepository.findAll(it.id, env, branch, tags) }

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
                        description = it.description,
                        parallel = it.parallel,
                        host = it.host,
                        endTime = it.endTime,
                        environment = it.environment,
                        branch = it.branch,
                        buildRef = it.buildRef,
                        tags = it.tags)
                    val executionStatsMono =
                        TestExecutionStats(executionId = testExecution.id, projectId = projectId).toMono()
                    _testExecutionStatsRepository.save(executionStatsMono)
                        .flatMap {
                            _testExecutionRepository.save(testExecution.toMono())
                        }
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
                                description = testExecutionModel.description,
                                parallel = testExecutionModel.parallel,
                                host = testExecutionModel.host,
                                startTime = it.startTime,
                                endTime = testExecutionModel.endTime,
                                environment = testExecutionModel.environment,
                                branch = testExecutionModel.branch,
                                buildRef = testExecutionModel.buildRef,
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

    override fun pushGroupId(executionId: String, groupId: String) {
        _testExecutionRepository.pushGroupId(executionId, groupId).subscribe()
    }

    override fun getStats(projectId: String, executionId: String): Mono<TestExecutionStats> =
        this.getExecutionById(projectId, executionId)
            .onErrorResume { throw it }
            .flatMap { _testExecutionStatsRepository.findByExecId(executionId) }

    override fun getFilters(projectId: String): Mono<TestExecutionFilters> =
        _projectService.getProject(projectId)
            .flatMap { buildFilters(projectId) }
            .switchIfEmpty(ProjectNotFoundException(projectId).toMono())

    override fun count(): Mono<Long> = _testExecutionRepository.count()

    private fun buildFilters(projectId: String): Mono<TestExecutionFilters> {
        val testExecutions = _testExecutionRepository.findAll(projectId)
        return Mono.zip(
            testExecutions.map { it.environment }.filter { it.isNotEmpty() }.distinct().collectList(),
            testExecutions.map { it.branch }.filter { it.isNotEmpty() }.distinct().collectList(),
            testExecutions.flatMap { it.tags.toFlux() }.filter { it.isNotEmpty() }.distinct().collectList()
        ).map { TestExecutionFilters(it.t1, it.t2, it.t3) }
    }
}