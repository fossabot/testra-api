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
import tech.testra.reportal.domain.entity.TestGroup
import tech.testra.reportal.domain.valueobjects.GroupType

@Repository
class TestGroupRepository : ITestGroupRepository {
    @Autowired
    lateinit var template: ReactiveMongoTemplate

    override fun save(testGroupMono: Mono<TestGroup>): Mono<TestGroup> = template.save(testGroupMono)

    override fun findById(id: String) = template.findById<TestGroup>(id)

    override fun findBy(name: String, projectId: String): Mono<TestGroup> {
        val criteria: Criteria = Criteria().andOperator(
            Criteria.where("projectId").isEqualTo(projectId),
            Criteria.where("name").isEqualTo(name)
        )
        return template.findOne(Query(criteria), TestGroup::class.java)
    }

    override fun findAll(): Flux<TestGroup> = template.findAll(TestGroup::class.java)

    override fun findAll(projectId: String): Flux<TestGroup> =
        template.find(Query(Criteria.where("projectId").isEqualTo(projectId)), TestGroup::class.java)

    override fun findAll(projectId: String, type: GroupType): Flux<TestGroup> {
        val criteria: Criteria = Criteria().andOperator(
            Criteria.where("projectId").isEqualTo(projectId),
            Criteria.where("type").isEqualTo(type)
        )
        return template.find(Query(criteria), TestGroup::class.java)
    }

    override fun deleteById(id: String): Mono<Boolean> =
        template.remove(Query(Criteria.where("id").isEqualTo(id)), TestGroup::class.java)
            .map { it.deletedCount > 0 }

    override fun count(): Mono<Long> = findAll().count()
}
