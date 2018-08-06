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
import tech.testra.reportal.api.rest.extensions.projectId
import tech.testra.reportal.api.rest.extensions.scenarioId
import tech.testra.reportal.api.rest.extensions.toListServerResponse
import tech.testra.reportal.exception.TestScenarioNotFoundException
import tech.testra.reportal.model.TestScenarioModel
import tech.testra.reportal.service.interfaces.ITestScenarioService

@Component
class TestScenarioHandler(private val _testScenarioService: ITestScenarioService) {

    fun findAll(req: ServerRequest): Mono<ServerResponse> =
        req.queryParam("featureId")
            .map { _testScenarioService.getScenariosByGroupId(req.projectId(), it).toListServerResponse() }
            .orElseGet { _testScenarioService.getScenariosByProjectId(req.projectId()).toListServerResponse() }

    fun findById(req: ServerRequest): Mono<ServerResponse> =
        _testScenarioService.getScenarioById(req.projectId(), req.scenarioId())
            .flatMap { ok().contentType(APPLICATION_JSON_UTF8).body(fromObject(it)) }

    fun create(req: ServerRequest): Mono<ServerResponse> =
        _testScenarioService.createScenario(req.projectId(), req.bodyToMono(TestScenarioModel::class.java))
            .flatMap { created(req.uri()).contentType(APPLICATION_JSON_UTF8).body(fromObject(it)) }

    fun update(req: ServerRequest): Mono<ServerResponse> =
        _testScenarioService.updateScenario(req.projectId(), req.scenarioId(),
            req.bodyToMono(TestScenarioModel::class.java))
            .flatMap { ok().contentType(APPLICATION_JSON_UTF8).body(fromObject(it)) }

    fun delete(req: ServerRequest): Mono<ServerResponse> =
        _testScenarioService.getScenarioById(req.projectId(), req.scenarioId())
            .switchIfEmpty(TestScenarioNotFoundException(req.scenarioId()).toMono())
            .flatMap { noContent().build(_testScenarioService.deleteScenarioById(req.scenarioId())) }
}