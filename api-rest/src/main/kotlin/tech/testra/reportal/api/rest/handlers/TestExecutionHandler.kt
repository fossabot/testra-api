package tech.testra.reportal.api.rest.handlers

import org.springframework.http.MediaType.APPLICATION_JSON_UTF8
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters.fromObject
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.ok
import reactor.core.publisher.Mono
import tech.testra.reportal.api.rest.extensions.executionId
import tech.testra.reportal.api.rest.extensions.projectId
import tech.testra.reportal.exception.TestExecutionNotFoundException
import tech.testra.reportal.model.TestExecutionModel
import tech.testra.reportal.service.interfaces.ITestExecutionService

@Component
class TestExecutionHandler(private val _testExecutionService: ITestExecutionService) {

    fun findAll(req: ServerRequest): Mono<ServerResponse> =
        _testExecutionService.getExecutionsByProjectId(req.projectId())
            .onErrorResume { throw it }
            .collectList()
            .flatMap { ok().contentType(APPLICATION_JSON_UTF8).body(fromObject(it)) }

    fun findById(req: ServerRequest): Mono<ServerResponse> =
        _testExecutionService.getExecutionById(req.projectId(), req.executionId())
            .flatMap { ok().contentType(APPLICATION_JSON_UTF8).body(fromObject(it)) }
            .onErrorResume { throw it }

    fun resultStats(req: ServerRequest): Mono<ServerResponse> =
        _testExecutionService.getStats(req.projectId(), req.executionId())
            .flatMap { ok().contentType(APPLICATION_JSON_UTF8).body(fromObject(it)) }
            .onErrorResume { throw it }

    fun createExecution(req: ServerRequest): Mono<ServerResponse> =
        _testExecutionService.createExecution(req.projectId(), req.bodyToMono(TestExecutionModel::class.java))
            .flatMap { ok().contentType(APPLICATION_JSON_UTF8).body(fromObject(it)) }
            .onErrorResume { throw it }

    fun updateExecution(req: ServerRequest): Mono<ServerResponse> =
        _testExecutionService.createExecution(req.projectId(), req.bodyToMono(TestExecutionModel::class.java))
            .flatMap { ok().contentType(APPLICATION_JSON_UTF8).body(fromObject(it)) }
            .onErrorResume { throw it }

    fun delete(req: ServerRequest): Mono<ServerResponse> {
        return _testExecutionService.deleteExecutionById(req.executionId())
            .flatMap {
                if (it) ok().build()
                else throw TestExecutionNotFoundException(req.executionId())
            }
    }
}