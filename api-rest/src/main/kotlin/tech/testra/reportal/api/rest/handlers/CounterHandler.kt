package tech.testra.reportal.api.rest.handlers

import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters.fromObject
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import tech.testra.reportal.model.CounterModel
import tech.testra.reportal.service.execution.TestExecutionService
import tech.testra.reportal.service.project.TestProjectService
import tech.testra.reportal.service.result.TestResultService
import tech.testra.reportal.service.scenario.TestScenarioService
import tech.testra.reportal.service.testcase.TestCaseService

@Component
class CounterHandler(
    val testProjectService: TestProjectService,
    val testScenarioService: TestScenarioService,
    val testCaseService: TestCaseService,
    val testExecutionService: TestExecutionService,
    val testResultService: TestResultService
) {

    fun get(req: ServerRequest): Mono<ServerResponse> {
        val counterModel = CounterModel(testProjectService.getSize(),
            testScenarioService.getSize(),
            testCaseService.getSize(),
            testExecutionService.getSize(),
            testResultService.getSize())

        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON_UTF8)
            .body(fromObject(counterModel))
    }
}