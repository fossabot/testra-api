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
import tech.testra.reportal.api.rest.extensions.getProjIdFromPath
import tech.testra.reportal.api.rest.extensions.getTestCaseIdFromPath
import tech.testra.reportal.api.rest.extensions.toListServerResponse
import tech.testra.reportal.exception.TestCaseNotFoundException
import tech.testra.reportal.model.TestCaseModel
import tech.testra.reportal.service.interfaces.ITestCaseService

@Component
class TestCaseHandler(val _testCaseService: ITestCaseService) {

    fun findAll(req: ServerRequest): Mono<ServerResponse> =
        req.queryParam("namespaceId")
            .map { _testCaseService.getTestCasesByGroupId(req.getProjIdFromPath(), it).toListServerResponse() }
            .orElseGet { _testCaseService.getTestCasesByProjectId(req.getProjIdFromPath()).toListServerResponse() }

    fun findById(req: ServerRequest): Mono<ServerResponse> =
        _testCaseService.getTestCaseById(req.getProjIdFromPath(), req.getTestCaseIdFromPath())
            .flatMap { ok().contentType(APPLICATION_JSON_UTF8).body(BodyInserters.fromObject(it)) }
            .onErrorResume { throw it }

    fun create(req: ServerRequest): Mono<ServerResponse> =
        _testCaseService.createTestCase(req.getProjIdFromPath(), req.bodyToMono(TestCaseModel::class.java))
            .flatMap { ok().contentType(APPLICATION_JSON_UTF8).body(fromObject(it)) }
            .onErrorResume { throw it }

    fun update(req: ServerRequest): Mono<ServerResponse> {
        return _testCaseService.updateTestCase(req.getProjIdFromPath(), req.getTestCaseIdFromPath(),
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