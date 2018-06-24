package tech.testra.reportal.api.rest.handlers

import org.springframework.http.MediaType.APPLICATION_JSON_UTF8
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters.fromObject
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.created
import org.springframework.web.reactive.function.server.ServerResponse.ok
import reactor.core.publisher.Mono
import tech.testra.reportal.api.rest.extensions.getProjIdFromPath
import tech.testra.reportal.api.rest.extensions.getScenarioIdFromPath
import tech.testra.reportal.api.rest.extensions.toListServerResponse
import tech.testra.reportal.exception.TestScenarioNotFoundException
import tech.testra.reportal.model.TestScenarioModel
import tech.testra.reportal.service.interfaces.ITestScenarioService

@Component
class TestScenarioHandler(val _testScenarioService: ITestScenarioService) {

    fun findAll(req: ServerRequest): Mono<ServerResponse> =
        req.queryParam("featureId")
            .map { _testScenarioService.getScenariosByGroupId(req.getProjIdFromPath(), it).toListServerResponse() }
            .orElseGet { _testScenarioService.getScenariosByProjectId(req.getProjIdFromPath()).toListServerResponse() }

    fun findById(req: ServerRequest): Mono<ServerResponse> =
        _testScenarioService.getScenarioById(req.getProjIdFromPath(), req.getScenarioIdFromPath())
            .flatMap { ok().contentType(APPLICATION_JSON_UTF8).body(fromObject(it)) }
            .onErrorResume { throw it }

    fun create(req: ServerRequest): Mono<ServerResponse> =
        _testScenarioService.createScenario(req.getProjIdFromPath(), req.bodyToMono(TestScenarioModel::class.java))
            .flatMap { created(req.uri()).contentType(APPLICATION_JSON_UTF8).body(fromObject(it)) }
            .onErrorResume { throw it }

    fun update(req: ServerRequest): Mono<ServerResponse> =
        _testScenarioService.updateScenario(req.getProjIdFromPath(), req.getScenarioIdFromPath(),
            req.bodyToMono(TestScenarioModel::class.java))
            .flatMap { ok().contentType(APPLICATION_JSON_UTF8).body(fromObject(it)) }
            .onErrorResume { throw it }

    fun delete(req: ServerRequest): Mono<ServerResponse> {
        return _testScenarioService.deleteScenarioById(req.getScenarioIdFromPath())
            .flatMap {
                if (it) ok().build()
                else throw TestScenarioNotFoundException(req.getScenarioIdFromPath())
            }
    }
}