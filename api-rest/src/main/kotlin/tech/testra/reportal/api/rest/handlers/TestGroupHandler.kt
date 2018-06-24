package tech.testra.reportal.api.rest.handlers

import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import tech.testra.reportal.api.rest.extensions.getExecIdFromPath
import tech.testra.reportal.api.rest.extensions.getProjIdFromPath
import tech.testra.reportal.api.rest.extensions.toListServerResponse
import tech.testra.reportal.service.interfaces.ITestGroupService

@Component
class TestGroupHandler(val _testGroupService: ITestGroupService) {

    fun findAll(req: ServerRequest): Mono<ServerResponse> =
        req.queryParam("type")
            .map { _testGroupService.getGroups(req.getProjIdFromPath(), it).toListServerResponse() }
            .orElseGet { _testGroupService.getGroups(req.getProjIdFromPath()).toListServerResponse() }

    fun findAllByExecId(req: ServerRequest): Mono<ServerResponse> =
        _testGroupService.getGroupsByExecutionId(req.getProjIdFromPath(), req.getExecIdFromPath())
            .toListServerResponse()
}