package tech.testra.reportal.repository

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.findById
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import tech.testra.reportal.domain.entity.Attachment

@Repository
class AttachmentRepository : IAttachmentRepository {
    @Autowired
    lateinit var template: ReactiveMongoTemplate

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override fun save(attachmentMono: Mono<Attachment>): Mono<Attachment> = template.save(attachmentMono)

    override fun findById(id: String) = template.findById<Attachment>(id)

    override fun findAll(): Flux<Attachment> = template.findAll(Attachment::class.java)

    override fun deleteById(id: String): Mono<Boolean> =
        template.remove(Query(Criteria.where("id").isEqualTo(id)), Attachment::class.java)
            .map { it.deletedCount > 0 }

    override fun size(): Mono<Long> = findAll().count()
}
