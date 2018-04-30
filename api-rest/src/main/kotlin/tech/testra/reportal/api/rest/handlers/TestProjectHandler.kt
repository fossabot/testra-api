package tech.testra.reportal.api.rest.handlers

import org.springframework.http.MediaType.APPLICATION_JSON_UTF8
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters.fromObject
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.created
import org.springframework.web.reactive.function.server.ServerResponse.ok
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono
import tech.testra.reportal.api.rest.extensions.getProjIdFromPath
import tech.testra.reportal.domain.entity.Project
import tech.testra.reportal.exception.ProjectNotFoundException
import tech.testra.reportal.model.ProjectModel
import tech.testra.reportal.service.interfaces.ITestProjectService

@Component
class TestProjectHandler(val _testProjectService: ITestProjectService) {

    fun getAllProjects(req: ServerRequest): Mono<ServerResponse> =
        ok().contentType(APPLICATION_JSON_UTF8)
            .body(_testProjectService.getProjects(), Project::class.java)

    fun getProjectById(req: ServerRequest): Mono<ServerResponse> =
        _testProjectService.getProjectById(req.getProjIdFromPath())
            .onErrorResume { it.toMono() }
            .flatMap { ok().contentType(APPLICATION_JSON_UTF8).body(fromObject(it)) }

    fun createProject(req: ServerRequest): Mono<ServerResponse> =
        _testProjectService.createProject(req.bodyToMono(ProjectModel::class.java))
            .flatMap { created(req.uri()).contentType(APPLICATION_JSON_UTF8).body(fromObject(it)) }

    fun updateProject(req: ServerRequest): Mono<ServerResponse> =
        _testProjectService.updateProject(req.getProjIdFromPath(), req.bodyToMono(ProjectModel::class.java))
            .flatMap { ok().contentType(APPLICATION_JSON_UTF8).body(fromObject(it)) }

    fun deleteProject(req: ServerRequest): Mono<ServerResponse> {
        return _testProjectService.deleteProjectById(req.getProjIdFromPath())
            .flatMap {
                if (it) ok().build()
                else throw ProjectNotFoundException(req.getProjIdFromPath())
            }
    }
}