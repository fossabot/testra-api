package tech.testra.reportal.service.attachment

import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono
import tech.testra.reportal.domain.entity.Attachment
import tech.testra.reportal.repository.IAttachmentRepository
import tech.testra.reportal.service.interfaces.IAttachmentService

@Service
class AttachmentService(val _attachmentRepository: IAttachmentRepository) : IAttachmentService {

    override fun getAttachment(id: String): Mono<Attachment> =
        _attachmentRepository.findById(id)

    override fun addAttachment(name: String, base64EncodedByteArray: String): Mono<Attachment> {
        val attachment = Attachment(name = name, base64EncodedByteArray = base64EncodedByteArray)
        return _attachmentRepository.save(attachment.toMono())
    }
}