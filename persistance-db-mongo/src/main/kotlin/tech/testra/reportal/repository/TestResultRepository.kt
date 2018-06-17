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

@Repository
class TestResultRepository : ITestResultRepository {

    @Autowired
    lateinit var template: ReactiveMongoTemplate

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override fun save(testResultMono: Mono<TestResult>): Mono<TestResult> = template.save(testResultMono)

    override fun findById(id: String) = template.findById<TestResult>(id)

    override fun findAll(): Flux<TestResult> = template.findAll(TestResult::class.java)

    override fun findAllByProjectIdAndExecutionId(projectId: String, executionId: String): Flux<TestResult> {
        val criteria: Criteria = Criteria().andOperator(
            Criteria.where("projectId").isEqualTo(projectId),
            Criteria.where("executionId").isEqualTo(executionId)
        )
        return template.find(Query(criteria), TestResult::class.java)
    }

    override fun deleteById(id: String): Mono<Boolean> =
            template.remove(Query(Criteria.where("id").isEqualTo(id)), TestResult::class.java)
                    .map { it.deletedCount > 0 }

    override fun size(): Mono<Long> = findAll().count()
}
