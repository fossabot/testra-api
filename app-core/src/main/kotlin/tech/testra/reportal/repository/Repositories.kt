package tech.testra.reportal.repository

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import tech.testra.reportal.domain.entity.Project
import tech.testra.reportal.domain.entity.TestCase
import tech.testra.reportal.domain.entity.TestExecution
import tech.testra.reportal.domain.entity.TestGroup
import tech.testra.reportal.domain.entity.TestResult
import tech.testra.reportal.domain.entity.TestScenario
import tech.testra.reportal.domain.valueobjects.GroupType
import tech.testra.reportal.domain.valueobjects.Result

interface IRepository<T> {
    fun save(executionMono: Mono<T>): Mono<T>
    fun findById(id: String): Mono<T>
    fun findAll(): Flux<T>
    fun deleteById(id: String): Mono<Boolean>
    fun size(): Mono<Long>
}

interface ITestProjectRepository : IRepository<Project> {
    fun findBy(name: String): Mono<Project>
}

interface ITestScenarioRepository : IRepository<TestScenario> {
    fun findAll(projectId: String): Flux<TestScenario>
    fun findBy(name: String, projectId: String, groupId: String): Flux<TestScenario>
}

interface ITestCaseRepository : IRepository<TestCase> {
    fun findAll(projectId: String): Flux<TestCase>
    fun findBy(name: String, projectId: String, groupId: String): Mono<TestCase>
}

interface ITestExecutionRepository : IRepository<TestExecution> {
    fun findAll(projectId: String): Flux<TestExecution>
    fun updateEndTime(id: String, endTime: Long): Mono<Boolean>
}

interface ITestResultRepository : IRepository<TestResult> {
    fun findAll(projectId: String, executionId: String): Flux<TestResult>
    fun findAll(projectId: String, executionId: String, result: Result): Flux<TestResult>
}

interface ITestGroupRepository : IRepository<TestGroup> {
    fun findBy(name: String, projectId: String): Mono<TestGroup>
    fun findAll(projectId: String): Flux<TestGroup>
    fun findAll(projectId: String, type: GroupType): Flux<TestGroup>
}