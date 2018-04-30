package tech.testra.reportal.api.rest.handlers

import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters.fromObject
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.created
import org.springframework.web.reactive.function.server.ServerResponse.ok
import reactor.core.publisher.Mono
import tech.testra.reportal.api.rest.extensions.getExecIdFromPath
import tech.testra.reportal.api.rest.extensions.getProjIdFromPath
import tech.testra.reportal.api.rest.extensions.getResultIdFromPath
import tech.testra.reportal.exception.TestResultNotFoundException
import tech.testra.reportal.model.TestResultModel
import tech.testra.reportal.service.interfaces.ITestResultService

@Component
class TestResultHandler(
    val _testResultHandler: ITestResultService
) {

    fun findAll(req: ServerRequest): Mono<ServerResponse> =
        _testResultHandler.getResultsByProjectIdAndExecutionId(req.getProjIdFromPath(), req.getExecIdFromPath())
            .onErrorResume { throw it }
            .collectList()
            .flatMap { ok().contentType(MediaType.APPLICATION_JSON_UTF8).body(fromObject(it)) }

    fun findById(req: ServerRequest): Mono<ServerResponse> =
        _testResultHandler.getResultById(req.getProjIdFromPath(), req.getExecIdFromPath(), req.getResultIdFromPath())
            .onErrorResume { throw it }
            .flatMap { ok().contentType(MediaType.APPLICATION_JSON_UTF8).body(fromObject(it)) }

    fun create(req: ServerRequest): Mono<ServerResponse> =
        _testResultHandler.createResult(req.getProjIdFromPath(), req.getExecIdFromPath(), req.bodyToMono(TestResultModel::class.java))
            .onErrorResume { throw it }
            .flatMap { created(req.uri()).contentType(MediaType.APPLICATION_JSON_UTF8).body(fromObject(it)) }

    fun update(req: ServerRequest): Mono<ServerResponse> =
        _testResultHandler.updateResult(
            req.getProjIdFromPath(),
            req.getExecIdFromPath(),
            req.getResultIdFromPath(),
            req.bodyToMono(TestResultModel::class.java)
        )
            .onErrorResume { throw it }
            .flatMap { ok().contentType(MediaType.APPLICATION_JSON_UTF8).body(fromObject(it)) }

    fun delete(req: ServerRequest): Mono<ServerResponse> {
        return _testResultHandler.deleteResultById(req.getResultIdFromPath())
            .flatMap {
                if (it) ok().build()
                else throw TestResultNotFoundException(req.getResultIdFromPath())
            }
    }
}