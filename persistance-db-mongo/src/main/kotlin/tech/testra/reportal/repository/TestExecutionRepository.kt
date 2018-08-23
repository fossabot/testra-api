package tech.testra.reportal.repository

import com.mongodb.client.result.UpdateResult
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
import tech.testra.reportal.domain.entity.TestExecution

@Repository
class TestExecutionRepository : ITestExecutionRepository {
    @Autowired
    lateinit var template: ReactiveMongoTemplate

    override fun save(executionMono: Mono<TestExecution>): Mono<TestExecution> = template.save(executionMono)

    override fun findById(id: String) = template.findById<TestExecution>(id)

    override fun findAll(): Flux<TestExecution> = template.findAll(TestExecution::class.java)

    override fun findAll(projectId: String): Flux<TestExecution> =
        template.find(Query(Criteria.where("projectId").isEqualTo(projectId)),
            TestExecution::class.java)

    override fun deleteById(id: String): Mono<Void> =
        template.remove(Query(Criteria.where("id").isEqualTo(id)), TestExecution::class.java).then()

    override fun deleteByProjectId(projectId: String): Mono<Void> =
        template.remove(Query(Criteria.where("projectId").isEqualTo(projectId)), TestExecution::class.java).then()

    override fun updateEndTime(id: String, endTime: Long): Mono<Boolean> {
        val update = Update()
        update.set("endTime", endTime)
        return template.updateFirst(Query(Criteria.where("id").isEqualTo(id)), update, TestExecution::class.java)
            .map { it.modifiedCount > 0 }
    }

    override fun count(): Mono<Long> = template.count(Query(), TestExecution::class.java)

    override fun pushGroupId(executionId: String, groupId: String): Mono<UpdateResult> =
        template.updateFirst(Query.query(Criteria.where("id").isEqualTo(executionId)),
            Update().addToSet("groupIds", groupId), TestExecution::class.java)
}
