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
import tech.testra.reportal.domain.entity.Simulation

@Repository
class SimulationRepository : ISimulationRepository {
    @Autowired
    lateinit var template: ReactiveMongoTemplate

    override fun save(simulationM: Mono<Simulation>): Mono<Simulation> =
        template.save(simulationM)

    override fun findById(id: String) = template.findById<Simulation>(id)

    override fun findAll(): Flux<Simulation> = template.findAll(Simulation::class.java)

    override fun findAll(projectId: String, executionId: String): Flux<Simulation> {
        val criteria: Criteria = Criteria().andOperator(
            Criteria.where("projectId").isEqualTo(projectId),
            Criteria.where("executionId").isEqualTo(executionId)
        )
        return template.find(Query(criteria), Simulation::class.java)
    }

    override fun deleteById(id: String): Mono<Void> =
        template.remove(Query(Criteria.where("id").isEqualTo(id)), Simulation::class.java).then()

    override fun deleteByProjectId(projectId: String): Mono<Void> =
        template.remove(Query(Criteria.where("projectId").isEqualTo(projectId)), Simulation::class.java).then()

    override fun count(): Mono<Long> = template.count(Query(), Simulation::class.java)
}
