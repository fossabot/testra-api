package tech.testra.reportal.api.rest.handlers

import org.springframework.http.MediaType.APPLICATION_JSON_UTF8
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters.fromObject
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.ok
import reactor.core.publisher.Mono
import tech.testra.reportal.api.rest.extensions.getProjIdFromPath
import tech.testra.reportal.service.interfaces.ITestGroupService

@Component
class TestGroupHandler(val _testGroupService: ITestGroupService) {

    fun findAll(req: ServerRequest): Mono<ServerResponse> =
        req.queryParam("type")
            .map {
                _testGroupService.getGroups(req.getProjIdFromPath(), it)
                    .onErrorResume { throw it }
                    .collectList()
                    .flatMap { ok().contentType(APPLICATION_JSON_UTF8).body(fromObject(it)) }
            }
            .orElseGet {
                _testGroupService.getGroups(req.getProjIdFromPath())
                    .onErrorResume { throw it }
                    .collectList()
                    .flatMap { ok().contentType(APPLICATION_JSON_UTF8).body(fromObject(it)) }
            }
}