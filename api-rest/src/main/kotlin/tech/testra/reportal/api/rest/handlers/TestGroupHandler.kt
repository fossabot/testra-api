package tech.testra.reportal.api.rest.handlers

import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import tech.testra.reportal.api.rest.extensions.executionId
import tech.testra.reportal.api.rest.extensions.projectId
import tech.testra.reportal.api.rest.extensions.toListServerResponse
import tech.testra.reportal.service.interfaces.ITestGroupService

@Component
class TestGroupHandler(private val _testGroupService: ITestGroupService) {

    fun findAll(req: ServerRequest): Mono<ServerResponse> =
        req.queryParam("type")
            .map { _testGroupService.getGroups(req.projectId(), it).toListServerResponse() }
            .orElseGet { _testGroupService.getGroups(req.projectId()).toListServerResponse() }

    fun findAllByExecId(req: ServerRequest): Mono<ServerResponse> =
        _testGroupService.getGroupsByExecutionId(req.projectId(), req.executionId())
            .toListServerResponse()
}