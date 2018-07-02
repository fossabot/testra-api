package tech.testra.reportal.api.rest.handlers

import org.springframework.http.MediaType.APPLICATION_JSON_UTF8
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters.fromPublisher
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.created
import org.springframework.web.reactive.function.server.ServerResponse.ok
import reactor.core.publisher.Mono
import tech.testra.reportal.api.rest.extensions.projectId
import tech.testra.reportal.domain.entity.Project
import tech.testra.reportal.exception.ProjectNotFoundException
import tech.testra.reportal.model.ProjectModel
import tech.testra.reportal.model.TestExecutionFilters
import tech.testra.reportal.service.interfaces.ITestExecutionService
import tech.testra.reportal.service.interfaces.ITestProjectService

@Component
class TestProjectHandler(
    private val _testProjectService: ITestProjectService,
    private val _testExecutionService: ITestExecutionService
) {

    fun getAllProjects(req: ServerRequest): Mono<ServerResponse> =
        ok().contentType(APPLICATION_JSON_UTF8)
            .body(_testProjectService.getProjects(), Project::class.java)

    fun getProjectById(req: ServerRequest): Mono<ServerResponse> =
        ok().contentType(APPLICATION_JSON_UTF8)
            .body(fromPublisher(_testProjectService.getProject(req.projectId()), Project::class.java))

    fun createProject(req: ServerRequest): Mono<ServerResponse> =
        created(req.uri()).contentType(APPLICATION_JSON_UTF8)
            .body(fromPublisher(
                _testProjectService.createProject(req.bodyToMono(ProjectModel::class.java)), Project::class.java)
            )

    fun updateProject(req: ServerRequest): Mono<ServerResponse> =
        ok().contentType(APPLICATION_JSON_UTF8)
            .body(fromPublisher(
                _testProjectService.updateProject(
                    req.projectId(), req.bodyToMono(ProjectModel::class.java)), Project::class.java)
            )

    fun deleteProject(req: ServerRequest): Mono<ServerResponse> {
        return _testProjectService.deleteProjectById(req.projectId())
            .flatMap {
                if (it) ok().build()
                else throw ProjectNotFoundException(req.projectId())
            }
    }

    fun executionFilters(req: ServerRequest): Mono<ServerResponse> =
        ok().contentType(APPLICATION_JSON_UTF8)
            .body(fromPublisher(_testExecutionService.getFilters(req.projectId()), TestExecutionFilters::class.java))
}