package tech.testra.reportal.api.rest.handlers

import org.springframework.http.MediaType.APPLICATION_JSON_UTF8
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.BodyInserters.fromObject
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.created
import org.springframework.web.reactive.function.server.ServerResponse.ok
import reactor.core.publisher.Mono
import tech.testra.reportal.api.rest.extensions.getProjectIdFromPath
import tech.testra.reportal.api.rest.extensions.getTestCaseIdFromPath
import tech.testra.reportal.exception.TestCaseNotFoundException
import tech.testra.reportal.model.TestCaseModel
import tech.testra.reportal.service.interfaces.ITestCaseService

@Component
class TestCaseHandler(val _testCaseService: ITestCaseService) {

    fun findAllByProjectId(req: ServerRequest): Mono<ServerResponse> =
        _testCaseService.getTestCasesByProjectId(req.getProjectIdFromPath())
            .onErrorResume { throw it }
            .collectList()
            .flatMap { ok().contentType(APPLICATION_JSON_UTF8).body(fromObject(it)) }

    fun findById(req: ServerRequest): Mono<ServerResponse> =
        _testCaseService.getTestCaseById(req.getProjectIdFromPath(), req.getTestCaseIdFromPath())
            .flatMap { ok().contentType(APPLICATION_JSON_UTF8).body(BodyInserters.fromObject(it)) }
            .onErrorResume { throw it }

    fun create(req: ServerRequest): Mono<ServerResponse> =
        _testCaseService.createTestCase(req.getProjectIdFromPath(), req.bodyToMono(TestCaseModel::class.java))
            .flatMap { ok().contentType(APPLICATION_JSON_UTF8).body(fromObject(it)) }
            .onErrorResume { throw it }

    fun update(req: ServerRequest): Mono<ServerResponse> {
        return _testCaseService.updateTestCase(req.getProjectIdFromPath(), req.getTestCaseIdFromPath(),
            req.bodyToMono(TestCaseModel::class.java))
            .flatMap { created(req.uri()).contentType(APPLICATION_JSON_UTF8).body(fromObject(it)) }
            .onErrorResume { throw it }
    }

    fun delete(req: ServerRequest): Mono<ServerResponse> {
        return _testCaseService.deleteTestCaseById(req.getTestCaseIdFromPath())
            .flatMap {
                if (it) ok().build()
                else throw TestCaseNotFoundException(req.getTestCaseIdFromPath())
            }
    }
}