package tech.testra.reportal.repository

import com.mongodb.client.result.UpdateResult
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import tech.testra.reportal.domain.entity.Project
import tech.testra.reportal.domain.entity.TestCase
import tech.testra.reportal.domain.entity.TestExecution
import tech.testra.reportal.domain.entity.TestExecutionStats
import tech.testra.reportal.domain.entity.TestGroup
import tech.testra.reportal.domain.entity.TestResult
import tech.testra.reportal.domain.entity.TestScenario
import tech.testra.reportal.domain.valueobjects.GroupType
import tech.testra.reportal.domain.valueobjects.ResultStatus

interface IRepository<T> {
    fun save(executionMono: Mono<T>): Mono<T>
    fun findById(id: String): Mono<T>
    fun findAll(): Flux<T>
    fun deleteById(id: String): Mono<Boolean>
    fun deleteByProjectId(projectId: String): Mono<Boolean>
    fun count(): Mono<Long>
}

interface ITestProjectRepository : IRepository<Project> {
    fun findBy(name: String): Mono<Project>
}

interface ITestScenarioRepository : IRepository<TestScenario> {
    fun findAllByProjectId(projectId: String): Flux<TestScenario>
    fun findAllByGroupId(groupId: String): Flux<TestScenario>
    fun findBy(name: String, projectId: String, groupId: String): Flux<TestScenario>
}

interface ITestCaseRepository : IRepository<TestCase> {
    fun findAll(projectId: String): Flux<TestCase>
    fun findBy(name: String, projectId: String, groupId: String): Flux<TestCase>
}

interface ITestExecutionRepository : IRepository<TestExecution> {
    fun findAll(projectId: String): Flux<TestExecution>
    fun findAll(projectId: String, env: String, branch: String, tags: List<String>): Flux<TestExecution>
    fun updateEndTime(id: String, endTime: Long): Mono<Boolean>
    fun pushGroupId(executionId: String, groupId: String): Mono<UpdateResult>
}

interface ITestExecutionStatsRepository : IRepository<TestExecutionStats> {
    fun findByExecId(executionId: String): Mono<TestExecutionStats>
    fun incPassedResults(executionId: String): Mono<Boolean>
    fun incFailedResults(executionId: String): Mono<Boolean>
    fun incOtherResults(executionId: String): Mono<Boolean>
    fun decPassedResults(executionId: String): Mono<Boolean>
    fun decFailedResults(executionId: String): Mono<Boolean>
    fun decOtherResults(executionId: String): Mono<Boolean>
}

interface ITestResultRepository : IRepository<TestResult> {
    fun findAll(projectId: String, executionId: String): Flux<TestResult>
    fun findAll(projectId: String, executionId: String, resultStatus: ResultStatus): Flux<TestResult>
    fun findAll(projectId: String, executionId: String, groupId: String): Flux<TestResult>
}

interface ITestGroupRepository : IRepository<TestGroup> {
    fun findBy(name: String, projectId: String): Mono<TestGroup>
    fun findAll(projectId: String): Flux<TestGroup>
    fun findAll(projectId: String, type: GroupType): Flux<TestGroup>
}