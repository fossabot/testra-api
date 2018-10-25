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
import tech.testra.reportal.domain.entity.ScanResult

@Repository
class ScanResultRepository : IScanResultRepository {
    @Autowired
    lateinit var template: ReactiveMongoTemplate

    override fun save(scanResult: Mono<ScanResult>): Mono<ScanResult> =
        template.save(scanResult)

    override fun findById(id: String): Mono<ScanResult> = template.findById(id)

    override fun findAll(): Flux<ScanResult> = template.findAll(ScanResult::class.java)

    override fun findAll(projectId: String, executionId: String): Flux<ScanResult> {
        val criteria: Criteria = Criteria().andOperator(
            Criteria.where("projectId").isEqualTo(projectId),
            Criteria.where("executionId").isEqualTo(executionId)
        )
        return template.find(Query(criteria), ScanResult::class.java)
    }

    override fun deleteById(id: String): Mono<Void> =
        template.remove(Query(Criteria.where("id").isEqualTo(id)), ScanResult::class.java).then()

    override fun deleteByProjectId(projectId: String): Mono<Void> =
        template.remove(Query(Criteria.where("projectId").isEqualTo(projectId)), ScanResult::class.java).then()

    override fun count(): Mono<Long> = template.count(Query(), ScanResult::class.java)
}
