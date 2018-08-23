package tech.testra.reportal.repository

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.findById
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import tech.testra.reportal.domain.entity.TestExecutionStats

@Repository
class TestExecutionStatsRepository : ITestExecutionStatsRepository {
    @Autowired
    lateinit var template: ReactiveMongoTemplate

    override fun save(esMono: Mono<TestExecutionStats>): Mono<TestExecutionStats> = template.save(esMono)

    override fun findById(id: String) = template.findById<TestExecutionStats>(id)

    override fun findAll(): Flux<TestExecutionStats> = template.findAll(TestExecutionStats::class.java)

    override fun deleteById(executionId: String): Mono<Void> =
        template.remove(Query(Criteria.where("executionId").isEqualTo(executionId)), TestExecutionStats::class.java)
            .then()

    override fun deleteByProjectId(projectId: String): Mono<Void> =
        template.remove(Query(Criteria.where("projectId").isEqualTo(projectId)), TestExecutionStats::class.java)
            .then()

    override fun findByExecId(executionId: String): Mono<TestExecutionStats> =
        template.findOne(Query(Criteria.where("executionId").isEqualTo(executionId)),
            TestExecutionStats::class.java)

    override fun incPassedResults(executionId: String): Mono<Boolean> =
        incOrDecResults("passed", 1, executionId)

    override fun incFailedResults(executionId: String): Mono<Boolean> =
        incOrDecResults("failed", 1, executionId)

    override fun incExpectedFailedResults(executionId: String): Mono<Boolean> =
        incOrDecResults("expectedFailures", 1, executionId)

    override fun incOtherResults(executionId: String): Mono<Boolean> =
        incOrDecResults("others", 1, executionId)

    override fun decPassedResults(executionId: String): Mono<Boolean> =
        incOrDecResults("passed", -1, executionId)

    override fun decFailedResults(executionId: String): Mono<Boolean> =
        incOrDecResults("failed", -1, executionId)

    override fun decExpectedFailedResults(executionId: String): Mono<Boolean> =
        incOrDecResults("expectedFailures", -1, executionId)

    override fun decOtherResults(executionId: String): Mono<Boolean> =
        incOrDecResults("others", -1, executionId)

    override fun count(): Mono<Long> = template.count(Query(), TestExecutionStats::class.java)

    private fun incOrDecResults(key: String, num: Number, executionId: String): Mono<Boolean> {
        val update = Update().inc(key, num)
        return template.updateFirst(Query(Criteria.where("executionId").isEqualTo(executionId)),
            update,
            TestExecutionStats::class.java)
            .map { it.modifiedCount > 0 }
    }
}
