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
import tech.testra.reportal.domain.entity.Project

@Repository
class TestProjectRepository : ITestProjectRepository {
    @Autowired
    lateinit var template: ReactiveMongoTemplate

    override fun save(projectMono: Mono<Project>): Mono<Project> = template.save(projectMono)

    override fun findById(id: String) = template.findById<Project>(id)

    override fun findAll(): Flux<Project> = template.findAll(Project::class.java)

    override fun deleteById(id: String): Mono<Boolean> =
            template.remove(Query(Criteria.where("id").isEqualTo(id)), Project::class.java)
                    .map { it.deletedCount > 0 }

    override fun findByName(name: String): Mono<Project> =
        template.findOne(Query(Criteria.where("name").isEqualTo(name)), Project::class.java)

    override fun size(): Mono<Long> = findAll().count()
}
