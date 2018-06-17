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
import tech.testra.reportal.domain.entity.TestExecution

@Repository
class TestExecutionRepository : ITestExecutionRepository {

    @Autowired
    lateinit var template: ReactiveMongoTemplate

    override fun save(entityMono: Mono<TestExecution>): Mono<TestExecution> = template.save(entityMono)

    override fun findById(id: String) = template.findById<TestExecution>(id)

    override fun findAll(): Flux<TestExecution> = template.findAll(TestExecution::class.java)

    override fun findAllByProjectId(projectId: String): Flux<TestExecution> =
        template.find(Query(Criteria.where("projectId").isEqualTo(projectId)),
            TestExecution::class.java)

    override fun deleteById(id: String): Mono<Boolean> =
            template.remove(Query(Criteria.where("id").isEqualTo(id)), TestExecution::class.java)
                    .map { it.deletedCount > 0 }

    override fun size(): Mono<Long> = findAll().count()
}
