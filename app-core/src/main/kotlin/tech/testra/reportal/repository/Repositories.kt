package tech.testra.reportal.repository

import com.mongodb.client.result.UpdateResult
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import tech.testra.reportal.domain.entity.Project
import tech.testra.reportal.domain.entity.ScanResult
import tech.testra.reportal.domain.entity.Simulation
import tech.testra.reportal.domain.entity.TestCase
import tech.testra.reportal.domain.entity.TestExecution
import tech.testra.reportal.domain.entity.TestExecutionStats
import tech.testra.reportal.domain.entity.TestGroup
import tech.testra.reportal.domain.entity.TestResult
import tech.testra.reportal.domain.entity.TestScenario
import tech.testra.reportal.domain.entity.Vulnerability
import tech.testra.reportal.domain.entity.VulnerabilityCategory
import tech.testra.reportal.domain.valueobjects.ExecutionCounter
import tech.testra.reportal.domain.valueobjects.GroupType
import tech.testra.reportal.domain.valueobjects.ResultStatus

interface IRepository<T> {
    fun save(entity: Mono<T>): Mono<T>
    fun findById(id: String): Mono<T>
    fun findAll(): Flux<T>
    fun deleteById(id: String): Mono<Void>
    fun count(): Mono<Long>
}

interface IDeletableByProject {
    fun deleteByProjectId(projectId: String): Mono<Void>
}

interface ICountableByProject {
    fun countByProjectId(projectId: String): Mono<Long>
}

interface ITestProjectRepository : IRepository<Project> {
    fun findBy(name: String): Mono<Project>
}

interface ITestScenarioRepository : IRepository<TestScenario>, IDeletableByProject, ICountableByProject {
    fun findAllByProjectId(projectId: String): Flux<TestScenario>
    fun findAllByGroupId(groupId: String): Flux<TestScenario>
    fun findBy(name: String, projectId: String, groupId: String): Flux<TestScenario>
}

interface ITestCaseRepository : IRepository<TestCase>, IDeletableByProject, ICountableByProject {
    fun findAll(projectId: String): Flux<TestCase>
    fun findBy(name: String, projectId: String, groupId: String): Flux<TestCase>
}

interface ISimulationRepository : IRepository<Simulation>, IDeletableByProject, ICountableByProject {
    fun findAll(projectId: String, executionId: String): Flux<Simulation>
}

interface IScanResultRepository : IRepository<ScanResult>, IDeletableByProject {
    fun findAll(projectId: String, executionId: String): Flux<ScanResult>
}

interface IVulnerabilityCategoryRepository : IRepository<VulnerabilityCategory>

interface IVulnerabilityRepository : IRepository<Vulnerability> {
    fun findByName(name: String): Mono<Vulnerability>
}

interface ITestExecutionRepository : IRepository<TestExecution>, IDeletableByProject, ICountableByProject {
    fun getRecentExecs(size: Int): Flux<TestExecution>
    fun getExecsCounts(size: Int): Flux<ExecutionCounter>
    fun findAll(projectId: String): Flux<TestExecution>
    fun updateEndTime(id: String, endTime: Long): Mono<Boolean>
    fun pushGroupId(executionId: String, groupId: String): Mono<UpdateResult>
}

interface ITestExecutionStatsRepository : IRepository<TestExecutionStats>, IDeletableByProject {
    fun findByExecId(executionId: String): Mono<TestExecutionStats>
    fun incPassedResults(executionId: String): Mono<Boolean>
    fun incFailedResults(executionId: String): Mono<Boolean>
    fun incExpectedFailedResults(executionId: String): Mono<Boolean>
    fun incOtherResults(executionId: String): Mono<Boolean>
    fun incManualResults(executionId: String): Mono<Boolean>
    fun decPassedResults(executionId: String): Mono<Boolean>
    fun decFailedResults(executionId: String): Mono<Boolean>
    fun decExpectedFailedResults(executionId: String): Mono<Boolean>
    fun decOtherResults(executionId: String): Mono<Boolean>
}

interface ITestResultRepository : IRepository<TestResult>, IDeletableByProject, ICountableByProject {
    fun findAll(projectId: String, executionId: String): Flux<TestResult>
    fun findAll(projectId: String, executionId: String, resultStatus: ResultStatus): Flux<TestResult>
    fun findAll(projectId: String, executionId: String, groupId: String): Flux<TestResult>
}

interface ITestGroupRepository : IRepository<TestGroup>, IDeletableByProject {
    fun findBy(name: String, description: String, projectId: String): Mono<TestGroup>
    fun findAll(projectId: String): Flux<TestGroup>
    fun findAll(projectId: String, type: GroupType): Flux<TestGroup>
}