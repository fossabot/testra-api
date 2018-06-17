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
import tech.testra.reportal.domain.entity.TestScenario

@Repository
class TestScenarioRepository : ITestScenarioRepository {
    @Autowired
    lateinit var template: ReactiveMongoTemplate

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override fun save(scenarioMono: Mono<TestScenario>): Mono<TestScenario> =
        template.save(scenarioMono)

    override fun findById(id: String) = template.findById<TestScenario>(id)

    override fun findAll(): Flux<TestScenario> = template.findAll(TestScenario::class.java)

    override fun findAllByProjectId(projectId: String): Flux<TestScenario> =
        template.find(Query(Criteria.where("projectId").isEqualTo(projectId)),
            TestScenario::class.java)

    override fun deleteById(id: String): Mono<Boolean> =
        template.remove(Query(Criteria.where("id").isEqualTo(id)), TestScenario::class.java)
            .map { it.deletedCount > 0 }

    override fun findByNameAndProjectIdAndGroupId(
        name: String,
        projectId: String,
        groupId: String
    ): Flux<TestScenario> {
        val criteria: Criteria = Criteria().andOperator(
            Criteria.where("projectId").isEqualTo(projectId),
            Criteria.where("featureId").isEqualTo(groupId),
            Criteria.where("name").isEqualTo(name)
        )

        return template.find(Query.query(criteria), TestScenario::class.java)
    }

    override fun size(): Mono<Long> = findAll().count()
}
