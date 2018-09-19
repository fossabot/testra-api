package tech.testra.reportal.service.project

import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono
import tech.testra.reportal.domain.entity.Project
import tech.testra.reportal.domain.valueobjects.ProjectType
import tech.testra.reportal.exception.ProjectAlreadyExistsException
import tech.testra.reportal.exception.ProjectNotFoundException
import tech.testra.reportal.extension.flatMapWithResumeOnError
import tech.testra.reportal.extension.onDuplicateKeyException
import tech.testra.reportal.extension.orElseGetException
import tech.testra.reportal.model.ProjectExecutionCounter
import tech.testra.reportal.model.ProjectModel
import tech.testra.reportal.repository.ITestCaseRepository
import tech.testra.reportal.repository.ITestExecutionRepository
import tech.testra.reportal.repository.ITestExecutionStatsRepository
import tech.testra.reportal.repository.ITestGroupRepository
import tech.testra.reportal.repository.ITestProjectRepository
import tech.testra.reportal.repository.ITestResultRepository
import tech.testra.reportal.repository.ITestScenarioRepository
import tech.testra.reportal.service.interfaces.ITestProjectService

@Service
class TestProjectService(
    private val _testProjectRepository: ITestProjectRepository,
    private val _testExecutionRepository: ITestExecutionRepository,
    private val _testResultRepository: ITestResultRepository,
    private val _testScenarioRepository: ITestScenarioRepository,
    private val _testCaseRepository: ITestCaseRepository,
    private val _testGroupRepository: ITestGroupRepository,
    private val _testExecutionStatsRepository: ITestExecutionStatsRepository
) : ITestProjectService {

    override fun getProjects() = _testProjectRepository.findAll()

    override fun getTopProjects(size: Int): Flux<ProjectExecutionCounter> =
        _testExecutionRepository.getExecsCounts(size)
            .flatMap {
                val execCount = it.total
                _testProjectRepository.findById(it.projectId)
                    .switchIfEmpty(ProjectNotFoundException(it.projectId).toMono())
                    .map { ProjectExecutionCounter(it.name, execCount) }
            }

    override fun getProject(idOrName: String): Mono<Project> =
        _testProjectRepository.findById(idOrName)
            .switchIfEmpty(_testProjectRepository.findBy(idOrName))
            .orElseGetException(ProjectNotFoundException(idOrName))

    override fun createProject(projectModelMono: Mono<ProjectModel>): Mono<Project> =
        projectModelMono
            .flatMapWithResumeOnError {
                saveProject(
                    Project(name = it.name,
                        projectType = ProjectType.valueOf(it.projectType.toString()),
                        description = it.description))
            }

    override fun updateProject(id: String, projectModelMono: Mono<ProjectModel>): Mono<Project> =
        getProject(id)
            .flatMapWithResumeOnError {
                projectModelMono.flatMap {
                    saveProject(Project(id = id,
                        name = it.name,
                        projectType = ProjectType.valueOf(it.projectType.toString()),
                        description = it.description))
                }
            }

    private fun saveProject(project: Project): Mono<Project> =
        _testProjectRepository.save(project.toMono())
            .flatMap { it.toMono() }
            .onDuplicateKeyException { ProjectAlreadyExistsException(project.name) }

    override fun deleteById(id: String): Mono<Void> =
        _testCaseRepository.deleteByProjectId(id).then()
            .then(_testScenarioRepository.deleteByProjectId(id))
            .then(_testGroupRepository.deleteByProjectId(id))
            .then(_testResultRepository.deleteByProjectId(id))
            .then(_testExecutionStatsRepository.deleteByProjectId(id))
            .then(_testExecutionRepository.deleteByProjectId(id))
            .then(_testProjectRepository.deleteById(id))

    override fun count(): Mono<Long> = _testProjectRepository.count()
}