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
import tech.testra.reportal.domain.valueobjects.ResultStatus

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

    override fun findAll(projectId: String, executionId: String, resultStatus: ResultStatus): Flux<TestResult> {
        val criteria: Criteria = Criteria().andOperator(
            Criteria.where("projectId").isEqualTo(projectId),
            Criteria.where("executionId").isEqualTo(executionId),
            Criteria.where("status").isEqualTo(resultStatus)
        )
        return findByQuery(Query(criteria))
    }

    override fun findAll(projectId: String, executionId: String, groupId: String): Flux<TestResult> {
        val criteria: Criteria = Criteria().andOperator(
            Criteria.where("projectId").isEqualTo(projectId),
            Criteria.where("executionId").isEqualTo(executionId),
            Criteria.where("groupId").isEqualTo(groupId)
        )
        return findByQuery(Query(criteria))
    }

    override fun deleteById(id: String): Mono<Void> =
        template.remove(Query(Criteria.where("id").isEqualTo(id)), TestResult::class.java).then()

    override fun deleteByProjectId(projectId: String): Mono<Void> =
        template.remove(Query(Criteria.where("projectId").isEqualTo(projectId)), TestResult::class.java).then()

    override fun count(): Mono<Long> = template.count(Query(), TestResult::class.java)

    override fun countByProjectId(projectId: String): Mono<Long> {
        val criteria = Criteria.where("projectId").isEqualTo(projectId)
        return template.count(Query(criteria), TestResult::class.java)
    }

    private fun findByQuery(q: Query) = template.find(q, TestResult::class.java)
}
