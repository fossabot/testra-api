package tech.testra.reportal.api.rest.handlers

import org.springframework.http.MediaType
import org.springframework.http.MediaType.APPLICATION_JSON_UTF8
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters.fromObject
import org.springframework.web.reactive.function.BodyInserters.fromPublisher
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.created
import org.springframework.web.reactive.function.server.ServerResponse.ok
import reactor.core.publisher.Mono
import tech.testra.reportal.api.rest.extensions.executionId
import tech.testra.reportal.api.rest.extensions.projectId
import tech.testra.reportal.domain.entity.ScanResult
import tech.testra.reportal.model.SecurityScanResult
import tech.testra.reportal.service.interfaces.ISecurityScanResultService

@Component
class SecurityScanResultHandler(val securityScanResultService: ISecurityScanResultService) {

    fun find(req: ServerRequest): Mono<ServerResponse> =
        ok().contentType(APPLICATION_JSON_UTF8)
            .body(fromPublisher(securityScanResultService.getScanResultsByProjectAndExecutionId(req.projectId(),
                req.executionId()), ScanResult::class.java))

    fun create(req: ServerRequest): Mono<ServerResponse> =
        securityScanResultService.createScanResult(req.projectId(), req.executionId(), req.bodyToMono(SecurityScanResult::class.java))
            .flatMap { created(req.uri()).contentType(MediaType.APPLICATION_JSON_UTF8).body(fromObject(it)) }
}