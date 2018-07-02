package tech.testra.reportal.api.rest.extensions

import org.springframework.http.MediaType
import org.springframework.web.reactive.function.BodyInserters.fromObject
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

fun ServerRequest.projectId(): String = this.pathVariable("projectId")
fun ServerRequest.executionId(): String = this.pathVariable("executionId")
fun ServerRequest.scenarioId(): String = this.pathVariable("scenarioId")
fun ServerRequest.resultId(): String = this.pathVariable("resultId")
fun ServerRequest.testCaseId(): String = this.pathVariable("testCaseId")

fun <T> Flux<T>.toListServerResponse(): Mono<ServerResponse> =
    this.onErrorResume { throw it }
        .collectList()
        .flatMap { ServerResponse.ok().contentType(MediaType.APPLICATION_JSON_UTF8).body(fromObject(it)) }