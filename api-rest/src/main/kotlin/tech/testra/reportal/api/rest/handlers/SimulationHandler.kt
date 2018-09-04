package tech.testra.reportal.api.rest.handlers

import org.springframework.http.MediaType
import org.springframework.http.MediaType.APPLICATION_JSON_UTF8
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters.fromObject
import org.springframework.web.reactive.function.BodyInserters.fromPublisher
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.created
import org.springframework.web.reactive.function.server.ServerResponse.noContent
import org.springframework.web.reactive.function.server.ServerResponse.ok
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono
import tech.testra.reportal.api.rest.extensions.executionId
import tech.testra.reportal.api.rest.extensions.projectId
import tech.testra.reportal.api.rest.extensions.resultId
import tech.testra.reportal.domain.entity.Simulation
import tech.testra.reportal.exception.TestResultNotFoundException
import tech.testra.reportal.model.SimulationModel
import tech.testra.reportal.service.interfaces.ISimulationService

@Component
class SimulationHandler(val simulationService: ISimulationService) {

    fun findAll(req: ServerRequest): Mono<ServerResponse> =
        ok().contentType(APPLICATION_JSON_UTF8)
            .body(fromPublisher(simulationService.getSimulationByProjectAndExecutionIds(req.projectId(),
                req.executionId()), Simulation::class.java))

    fun create(req: ServerRequest): Mono<ServerResponse> =
        simulationService.createSimulation(req.projectId(), req.executionId(), req.bodyToMono(SimulationModel::class.java))
            .flatMap { created(req.uri()).contentType(MediaType.APPLICATION_JSON_UTF8).body(fromObject(it)) }

    fun delete(req: ServerRequest): Mono<ServerResponse> =
        simulationService.getResultById(req.projectId(), req.executionId(), req.resultId())
            .switchIfEmpty(TestResultNotFoundException(req.resultId()).toMono())
            .flatMap { noContent().build(simulationService.deleteSimulationById(req.resultId())) }
}