package tech.testra.reportal.api.rest.extensions

import org.springframework.web.reactive.function.server.ServerRequest

fun ServerRequest.getProjIdFromPath() = this.pathVariable("projectId")
fun ServerRequest.getExecIdFromPath() = this.pathVariable("executionId")
fun ServerRequest.getScenarioIdFromPath() = this.pathVariable("scenarioId")
fun ServerRequest.getResultIdFromPath() = this.pathVariable("resultId")
fun ServerRequest.getTestCaseIdFromPath() = this.pathVariable("testCaseId")