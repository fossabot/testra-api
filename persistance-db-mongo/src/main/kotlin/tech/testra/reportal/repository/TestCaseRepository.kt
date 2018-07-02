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
import tech.testra.reportal.domain.entity.TestCase

@Repository
class TestCaseRepository : ITestCaseRepository {
    @Autowired
    lateinit var template: ReactiveMongoTemplate

    override fun save(scenarioMono: Mono<TestCase>): Mono<TestCase> =
        template.save(scenarioMono)

    override fun findById(id: String) = template.findById<TestCase>(id)

    override fun findAll(): Flux<TestCase> = template.findAll(TestCase::class.java)

    override fun findAll(projectId: String): Flux<TestCase> =
        template.find(Query(Criteria.where("projectId").isEqualTo(projectId)),
            TestCase::class.java)

    override fun deleteById(id: String): Mono<Boolean> =
        template.remove(Query(Criteria.where("id").isEqualTo(id)), TestCase::class.java)
            .map { it.deletedCount > 0 }

    override fun findBy(
        name: String,
        projectId: String,
        groupId: String
    ): Mono<TestCase> {
        val criteria: Criteria = Criteria().andOperator(
            Criteria.where("projectId").isEqualTo(projectId),
            Criteria.where("groupId").isEqualTo(groupId),
            Criteria.where("name").isEqualTo(name)
        )
        return template.findOne(Query(criteria), TestCase::class.java)
    }

    override fun count(): Mono<Long> = findAll().count()
}
