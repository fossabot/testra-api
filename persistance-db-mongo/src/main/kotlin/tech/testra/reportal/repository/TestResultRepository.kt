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
import tech.testra.reportal.domain.entity.TestResult
import tech.testra.reportal.domain.valueobjects.Result

@Repository
class TestResultRepository : ITestResultRepository {

    @Autowired
    lateinit var template: ReactiveMongoTemplate

    override fun save(testResultMono: Mono<TestResult>): Mono<TestResult> = template.save(testResultMono)

    override fun findById(id: String) = template.findById<TestResult>(id)

    override fun findAll(): Flux<TestResult> = template.findAll(TestResult::class.java)

    override fun findAll(projectId: String, executionId: String): Flux<TestResult> {
        val criteria: Criteria = Criteria().andOperator(
            Criteria.where("projectId").isEqualTo(projectId),
            Criteria.where("executionId").isEqualTo(executionId)
        )
        return findByQuery(Query(criteria))
    }

    override fun findAll(projectId: String, executionId: String, result: Result): Flux<TestResult> {
        val criteria: Criteria = Criteria().andOperator(
            Criteria.where("projectId").isEqualTo(projectId),
            Criteria.where("executionId").isEqualTo(executionId),
            Criteria.where("result").isEqualTo(result)
        )
        return findByQuery(Query(criteria))
    }

    override fun deleteById(id: String): Mono<Boolean> =
        template.remove(Query(Criteria.where("id").isEqualTo(id)), TestResult::class.java)
            .map { it.deletedCount > 0 }

    override fun size(): Mono<Long> = findAll().count()

    private fun findByQuery(q: Query) = template.find(q, TestResult::class.java)
}
