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
class TestProjectService(val _testProjectRepository: ITestProjectRepository) : ITestProjectService {
    override fun getProjects() = _testProjectRepository.findAll()

    override fun getProjectById(id: String): Mono<Project> =
        _testProjectRepository.findById(id)
            .orElseGetException(ProjectNotFoundException(id))

    override fun createProject(projectModelMono: Mono<ProjectModel>): Mono<Project> =
        projectModelMono.flatMapWithResumeOnError { saveProject(Project(name = it.name)) }

    override fun updateProject(id: String, projectModelMono: Mono<ProjectModel>): Mono<Project> =
        getProjectById(id)
            .flatMapWithResumeOnError {
                projectModelMono.flatMap { saveProject(Project(id = id, name = it.name)) }
            }

    private fun saveProject(project: Project): Mono<Project> =
        _testProjectRepository.save(project.toMono())
            .flatMap { it.toMono() }
            .onDuplicateKeyException { ProjectAlreadyExistsException(project.name) }

    override fun deleteProjectById(id: String) = _testProjectRepository.deleteById(id)

    override fun getSize(): Long =
        _testProjectRepository.size().blockOptional()
            .map { it }
            .orElse(-1L)
}