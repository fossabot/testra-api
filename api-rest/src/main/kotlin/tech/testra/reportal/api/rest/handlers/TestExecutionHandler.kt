package tech.testra.reportal.api.rest.handlers

import org.springframework.http.MediaType.APPLICATION_JSON_UTF8
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters.fromObject
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.created
import org.springframework.web.reactive.function.server.ServerResponse.noContent
import org.springframework.web.reactive.function.server.ServerResponse.ok
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono
import tech.testra.reportal.api.rest.extensions.executionId
import tech.testra.reportal.api.rest.extensions.projectId
import tech.testra.reportal.exception.QueryParamMissingException
import tech.testra.reportal.exception.TestExecutionNotFoundException
import tech.testra.reportal.model.TestExecutionModel
import tech.testra.reportal.service.interfaces.ITestExecutionService

@Component
class TestExecutionHandler(private val _testExecutionService: ITestExecutionService) {

    fun findAll(req: ServerRequest): Mono<ServerResponse> =
        _testExecutionService.getExecutionsByProjectId(req.projectId())
            .collectList()
            .flatMap { ok().contentType(APPLICATION_JSON_UTF8).body(fromObject(it)) }

    fun getRecents(req: ServerRequest): Mono<ServerResponse> =
        req.queryParam("size")
            .map {
                _testExecutionService.getRecentExecutions(it.toInt())
                    .collectList()
                    .flatMap { ok().contentType(APPLICATION_JSON_UTF8).body(fromObject(it)) }
            }.orElseThrow { QueryParamMissingException("size") }

    fun findById(req: ServerRequest): Mono<ServerResponse> =
        _testExecutionService.getExecutionById(req.projectId(), req.executionId())
            .flatMap { ok().contentType(APPLICATION_JSON_UTF8).body(fromObject(it)) }

    fun resultStats(req: ServerRequest): Mono<ServerResponse> =
        _testExecutionService.getStats(req.projectId(), req.executionId())
            .flatMap { ok().contentType(APPLICATION_JSON_UTF8).body(fromObject(it)) }

    fun createExecution(req: ServerRequest): Mono<ServerResponse> =
        _testExecutionService.createExecution(req.projectId(), req.bodyToMono(TestExecutionModel::class.java))
            .flatMap { created(req.uri()).contentType(APPLICATION_JSON_UTF8).body(fromObject(it)) }

    fun updateExecution(req: ServerRequest): Mono<ServerResponse> =
        _testExecutionService.createExecution(req.projectId(), req.bodyToMono(TestExecutionModel::class.java))
            .flatMap { ok().contentType(APPLICATION_JSON_UTF8).body(fromObject(it)) }

    fun delete(req: ServerRequest): Mono<ServerResponse> =
        _testExecutionService.getExecutionById(req.projectId(), req.executionId())
            .switchIfEmpty(TestExecutionNotFoundException(req.executionId()).toMono())
            .flatMap { noContent().build(_testExecutionService.deleteExecutionById(req.executionId())) }
}