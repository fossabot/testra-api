package tech.testra.reportal.service.project

import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono
import tech.testra.reportal.domain.entity.Project
import tech.testra.reportal.exception.ProjectAlreadyExistsException
import tech.testra.reportal.exception.ProjectNotFoundException
import tech.testra.reportal.extension.flatMapWithResumeOnError
import tech.testra.reportal.extension.onDuplicateKeyException
import tech.testra.reportal.extension.orElseGetException
import tech.testra.reportal.model.ProjectModel
import tech.testra.reportal.repository.ITestProjectRepository
import tech.testra.reportal.service.interfaces.ITestProjectService

@Service
class TestProjectService(private val _testProjectRepository: ITestProjectRepository) : ITestProjectService {
    override fun getProjects() = _testProjectRepository.findAll()

    override fun getProject(idOrName: String): Mono<Project> =
        _testProjectRepository.findById(idOrName)
            .switchIfEmpty(_testProjectRepository.findBy(idOrName))
            .orElseGetException(ProjectNotFoundException(idOrName))

    override fun createProject(projectModelMono: Mono<ProjectModel>): Mono<Project> =
        projectModelMono
            .flatMapWithResumeOnError {
                saveProject(Project(name = it.name, description = it.description))
            }

    override fun updateProject(id: String, projectModelMono: Mono<ProjectModel>): Mono<Project> =
        getProject(id)
            .flatMapWithResumeOnError {
                projectModelMono.flatMap { saveProject(Project(id = id, name = it.name, description = it.description)) }
            }

    private fun saveProject(project: Project): Mono<Project> =
        _testProjectRepository.save(project.toMono())
            .flatMap { it.toMono() }
            .onDuplicateKeyException { ProjectAlreadyExistsException(project.name) }

    override fun deleteProjectById(id: String) = _testProjectRepository.deleteById(id)

    override fun count(): Mono<Long> = _testProjectRepository.count()
}