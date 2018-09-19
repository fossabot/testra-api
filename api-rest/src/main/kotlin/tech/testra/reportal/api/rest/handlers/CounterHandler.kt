package tech.testra.reportal.api.rest.handlers

import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters.fromPublisher
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.ok
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono
import tech.testra.reportal.model.CounterModel
import tech.testra.reportal.service.execution.TestExecutionService
import tech.testra.reportal.service.project.TestProjectService
import tech.testra.reportal.service.result.TestResultService
import tech.testra.reportal.service.scenario.TestScenarioService
import tech.testra.reportal.service.simulation.SimulationService
import tech.testra.reportal.service.testcase.TestCaseService

@Component
class CounterHandler(
    private val testProjectService: TestProjectService,
    private val testScenarioService: TestScenarioService,
    private val testCaseService: TestCaseService,
    private val testExecutionService: TestExecutionService,
    private val testResultService: TestResultService,
    private val simulationService: SimulationService
) {
    val ZERO: Long = 0

    fun get(req: ServerRequest): Mono<ServerResponse> =
        ok().contentType(MediaType.APPLICATION_JSON_UTF8)
            .body(fromPublisher(counterModelPublisher(), CounterModel::class.java))

    private fun counterModelPublisher() =
        Mono.zip(
            Mono.zip(
                testProjectService.count(),
                testScenarioService.count(),
                testCaseService.count(),
                testExecutionService.count(),
                testResultService.count(),
                simulationService.count()
            ).map { CounterModel(it.t1, it.t2, it.t3, it.t4, it.t5, it.t6) },
            ZERO.toMono()
        ).map { it.t1.copy(vulnerabilityAlertsCount = it.t2) }
}