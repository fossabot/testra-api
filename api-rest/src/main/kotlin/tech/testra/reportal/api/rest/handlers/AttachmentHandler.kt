package tech.testra.reportal.api.rest.handlers

import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters.fromObject
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import tech.testra.reportal.api.rest.extensions.getAttachmentIdFromPath
import tech.testra.reportal.service.attachment.AttachmentService

@Component
class AttachmentHandler(val _attachmentService: AttachmentService) {

    fun get(req: ServerRequest): Mono<ServerResponse> {
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON_UTF8)
            .body(fromObject(_attachmentService.getAttachment(req.getAttachmentIdFromPath())))
    }
}
