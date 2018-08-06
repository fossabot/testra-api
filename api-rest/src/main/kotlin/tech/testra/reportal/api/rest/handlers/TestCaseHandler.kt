package tech.testra.reportal.api.rest.handlers

import org.springframework.http.MediaType.APPLICATION_JSON_UTF8
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.BodyInserters.fromObject
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.created
import org.springframework.web.reactive.function.server.ServerResponse.noContent
import org.springframework.web.reactive.function.server.ServerResponse.ok
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono
import tech.testra.reportal.api.rest.extensions.projectId
import tech.testra.reportal.api.rest.extensions.testCaseId
import tech.testra.reportal.api.rest.extensions.toListServerResponse
import tech.testra.reportal.exception.TestCaseNotFoundException
import tech.testra.reportal.model.TestCaseModel
import tech.testra.reportal.service.interfaces.ITestCaseService

@Component
class TestCaseHandler(private val _testCaseService: ITestCaseService) {

    fun findAll(req: ServerRequest): Mono<ServerResponse> =
        req.queryParam("namespaceId")
            .map { _testCaseService.getTestCasesByGroupId(req.projectId(), it).toListServerResponse() }
            .orElseGet { _testCaseService.getTestCasesByProjectId(req.projectId()).toListServerResponse() }

    fun findById(req: ServerRequest): Mono<ServerResponse> =
        _testCaseService.getTestCaseById(req.projectId(), req.testCaseId())
            .flatMap { ok().contentType(APPLICATION_JSON_UTF8).body(BodyInserters.fromObject(it)) }

    fun create(req: ServerRequest): Mono<ServerResponse> =
        _testCaseService.createTestCase(req.projectId(), req.bodyToMono(TestCaseModel::class.java))
            .flatMap { ok().contentType(APPLICATION_JSON_UTF8).body(fromObject(it)) }

    fun update(req: ServerRequest): Mono<ServerResponse> {
        return _testCaseService.updateTestCase(req.projectId(), req.testCaseId(),
            req.bodyToMono(TestCaseModel::class.java))
            .flatMap { created(req.uri()).contentType(APPLICATION_JSON_UTF8).body(fromObject(it)) }
    }

    fun delete(req: ServerRequest): Mono<ServerResponse> =
        _testCaseService.getTestCaseById(req.projectId(), req.testCaseId())
            .switchIfEmpty(TestCaseNotFoundException(req.testCaseId()).toMono())
            .flatMap { noContent().build(_testCaseService.deleteTestCaseById(req.testCaseId())) }
}