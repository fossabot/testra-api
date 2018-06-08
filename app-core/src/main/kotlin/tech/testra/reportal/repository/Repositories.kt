package tech.testra.reportal.repository

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import tech.testra.reportal.domain.entity.Project
import tech.testra.reportal.domain.entity.TestCase
import tech.testra.reportal.domain.entity.TestExecution
import tech.testra.reportal.domain.entity.TestGroup
import tech.testra.reportal.domain.entity.TestResult
import tech.testra.reportal.domain.entity.TestScenario

interface IRepository<T> {
    fun save(executionMono: Mono<T>): Mono<T>
    fun findById(id: String): Mono<T>
    fun findAll(): Flux<T>
    fun deleteById(id: String): Mono<Boolean>
    fun size(): Mono<Long>
}

interface ITestProjectRepository : IRepository<Project> {
    fun findByName(name: String): Mono<Project>
}

interface ITestScenarioRepository : IRepository<TestScenario> {
    fun findAllByProjectId(projectId: String): Flux<TestScenario>
    fun findByNameAndProjectIdAndGroupId(name: String, projectId: String, groupId: String): Flux<TestScenario>
}

interface ITestCaseRepository : IRepository<TestCase> {
    fun findAllByProjectId(projectId: String): Flux<TestCase>
    fun findByNameAndProjectIdAndGroupId(name: String, projectId: String, groupId: String): Mono<TestCase>
}

interface ITestExecutionRepository : IRepository<TestExecution> {
    fun findAllByProjectId(projectId: String): Flux<TestExecution>
}

interface ITestResultRepository : IRepository<TestResult> {
    fun findAllByProjectIdAndExecutionId(projectId: String, executionId: String): Flux<TestResult>
}

interface ITestGroupRepository : IRepository<TestGroup> {
    fun findByNameAndProjectId(name: String, projectId: String): Mono<TestGroup>
}