package tech.testra.reportal.api.rest.extensions

import org.springframework.web.reactive.function.server.ServerRequest

fun ServerRequest.getProjectIdFromPath(): String = this.pathVariable("projectId")
fun ServerRequest.getExecIdFromPath(): String = this.pathVariable("executionId")
fun ServerRequest.getScenarioIdFromPath(): String = this.pathVariable("scenarioId")
fun ServerRequest.getResultIdFromPath(): String = this.pathVariable("resultId")
fun ServerRequest.getTestCaseIdFromPath(): String = this.pathVariable("testCaseId")
fun ServerRequest.getAttachmentIdFromPath(): String = this.pathVariable("attachmentId")