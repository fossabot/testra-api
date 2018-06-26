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

    override fun deleteById(executionId: String): Mono<Boolean> =
        template.remove(Query(Criteria.where("executionId").isEqualTo(executionId)), TestExecutionStats::class.java)
            .map { it.deletedCount > 0 }

    override fun findByExecId(executionId: String): Mono<TestExecutionStats> =
        template.findOne(Query(Criteria.where("executionId").isEqualTo(executionId)),
            TestExecutionStats::class.java)

    override fun incPassedResults(executionId: String): Mono<Boolean> =
        incOrDecResults("passedResults", 1, executionId)

    override fun incFailedResults(executionId: String): Mono<Boolean> =
        incOrDecResults("failedResults", 1, executionId)

    override fun incOtherResults(executionId: String): Mono<Boolean> =
        incOrDecResults("otherResults", 1, executionId)

    override fun decPassedResults(executionId: String): Mono<Boolean> =
        incOrDecResults("passedResults", -1, executionId)

    override fun decFailedResults(executionId: String): Mono<Boolean> =
        incOrDecResults("failedResults", -1, executionId)

    override fun decOtherResults(executionId: String): Mono<Boolean> =
        incOrDecResults("otherResults", -1, executionId)

    override fun size(): Mono<Long> = findAll().count()

    private fun incOrDecResults(key: String, num: Number, executionId: String): Mono<Boolean> {
        val update = Update().inc(key, num)
        return template.updateFirst(Query(Criteria.where("executionId").isEqualTo(executionId)),
            update,
            TestExecutionStats::class.java)
            .map { it.modifiedCount > 0 }
    }
}
