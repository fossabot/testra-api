package tech.testra.reportal.api.rest.handlers

import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters.fromObject
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.created
import org.springframework.web.reactive.function.server.ServerResponse.noContent
import org.springframework.web.reactive.function.server.ServerResponse.ok
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono
import tech.testra.reportal.api.rest.extensions.executionId
import tech.testra.reportal.api.rest.extensions.projectId
import tech.testra.reportal.api.rest.extensions.resultId
import tech.testra.reportal.exception.TestResultNotFoundException
import tech.testra.reportal.model.EnrichedTestResultModel
import tech.testra.reportal.model.ResultStatus
import tech.testra.reportal.model.TestResultModel
import tech.testra.reportal.service.interfaces.ITestResultService

@Component
class TestResultHandler(val _testResultService: ITestResultService) {

    fun findAll(req: ServerRequest): Mono<ServerResponse> {
        val projectId = req.projectId()
        val execId = req.executionId()
        return req.queryParam("status")
            .map { getResults { _testResultService.getResults(projectId, execId, ResultStatus.valueOf(it)) } }
            .orElseGet {
                req.queryParam("groupId")
                    .map { getResults { _testResultService.getResults(projectId, execId, it) } }
                    .orElseGet { getResults { _testResultService.getResults(projectId, execId) } }
            }
    }

    fun findById(req: ServerRequest): Mono<ServerResponse> =
        _testResultService.getResultById(req.projectId(), req.executionId(), req.resultId())
            .flatMap { ok().contentType(MediaType.APPLICATION_JSON_UTF8).body(fromObject(it)) }

    fun create(req: ServerRequest): Mono<ServerResponse> =
        _testResultService.createResult(req.projectId(), req.executionId(), req.bodyToMono(TestResultModel::class.java))
            .flatMap { created(req.uri()).contentType(MediaType.APPLICATION_JSON_UTF8).body(fromObject(it)) }

    fun update(req: ServerRequest): Mono<ServerResponse> =
        _testResultService.updateResult(
            req.projectId(),
            req.executionId(),
            req.resultId(),
            req.bodyToMono(TestResultModel::class.java)
        )
            .flatMap { ok().contentType(MediaType.APPLICATION_JSON_UTF8).body(fromObject(it)) }

    fun delete(req: ServerRequest): Mono<ServerResponse> =
        _testResultService.getResultById(req.projectId(), req.executionId(), req.resultId())
            .switchIfEmpty(TestResultNotFoundException(req.resultId()).toMono())
            .flatMap { noContent().build(_testResultService.deleteResultById(req.resultId())) }

    private fun getResults(f: () -> Flux<EnrichedTestResultModel>): Mono<ServerResponse> =
        f.invoke()
            .collectList()
            .flatMap { ok().contentType(MediaType.APPLICATION_JSON_UTF8).body(fromObject(it)) }
}