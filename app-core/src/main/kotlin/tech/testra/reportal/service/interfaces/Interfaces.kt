package tech.testra.reportal.service.interfaces

import org.springframework.stereotype.Service
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
import tech.testra.reportal.model.EnrichedTestResultModel
import tech.testra.reportal.model.ProjectModel
import tech.testra.reportal.model.TestCaseModel
import tech.testra.reportal.model.TestExecutionFilters
import tech.testra.reportal.model.TestExecutionModel
import tech.testra.reportal.model.TestResultModel
import tech.testra.reportal.model.TestScenarioModel
import tech.testra.reportal.model.ResultStatus as ResultInModel

@Service
interface ITestProjectService {
    fun getProjects(): Flux<Project>
    fun getProject(idOrName: String): Mono<Project>
    fun createProject(projectModelMono: Mono<ProjectModel>): Mono<Project>
    fun updateProject(id: String, projectModelMono: Mono<ProjectModel>): Mono<Project>
    fun deleteById(id: String): Mono<Void>
    fun count(): Mono<Long>
}

@Service
interface ITestScenarioService {
    fun getScenariosByProjectId(projectId: String): Flux<TestScenario>
    fun getScenariosByGroupId(projectId: String, groupId: String): Flux<TestScenario>
    fun getScenarioById(projectId: String, scenarioId: String): Mono<TestScenario>
    fun createScenario(projectId: String, testScenarioModelMono: Mono<TestScenarioModel>): Mono<TestScenario>
    fun updateScenario(
        projectId: String,
        scenarioId: String,
        testScenarioModelMono: Mono<TestScenarioModel>
    ): Mono<TestScenario>

    fun deleteScenarioById(id: String): Mono<Void>
    fun count(): Mono<Long>
}

@Service
interface ITestCaseService {
    fun getTestCasesByProjectId(projectId: String): Flux<TestCase>
    fun getTestCaseById(projectId: String, testCaseId: String): Mono<TestCase>
    fun createTestCase(projectId: String, testCaseModelMono: Mono<TestCaseModel>): Mono<TestCase>
    fun updateTestCase(
        projectId: String,
        testCaseId: String,
        testCaseModelMono: Mono<TestCaseModel>
    ): Mono<TestCase>

    fun deleteTestCaseById(id: String): Mono<Void>
    fun count(): Mono<Long>
    fun getTestCasesByGroupId(projectId: String, groupId: String): Flux<TestCase>
}

@Service
interface ITestExecutionService {
    fun getExecutionsByProjectId(projectId: String): Flux<TestExecution>
    fun getExecutionById(projectId: String, executionId: String): Mono<TestExecution>
    fun createExecution(projectId: String, testExecutionModelMono: Mono<TestExecutionModel>): Mono<TestExecution>
    fun updateExecution(
        projectId: String,
        executionId: String,
        testExecutionModelMono: Mono<TestExecutionModel>
    ): Mono<TestExecution>

    fun updateEndTime(id: String, endTime: Long)
    fun deleteExecutionById(id: String): Mono<Void>
    fun pushGroupId(executionId: String, groupId: String)
    fun getStats(projectId: String, executionId: String): Mono<TestExecutionStats>
    fun getFilters(projectId: String): Mono<TestExecutionFilters>
    fun count(): Mono<Long>
}

@Service
interface ITestResultService {
    fun getResults(projectId: String, executionId: String): Flux<EnrichedTestResultModel>
    fun getResults(projectId: String, executionId: String, status: ResultInModel): Flux<EnrichedTestResultModel>
    fun getResults(projectId: String, executionId: String, groupId: String): Flux<EnrichedTestResultModel>
    fun getResultById(projectId: String, executionId: String, resultId: String): Mono<TestResult>
    fun createResult(
        projectId: String,
        executionId: String,
        testResultModelMono: Mono<TestResultModel>
    ): Mono<TestResult>

    fun updateResult(
        projectId: String,
        executionId: String,
        resultId: String,
        testResultModelMono: Mono<TestResultModel>
    ): Mono<TestResult>

    fun deleteResultById(id: String): Mono<Void>
    fun count(): Mono<Long>
}

interface ITestGroupService {
    fun getOrAddGroup(
        groupName: String,
        groupDescription: String = "",
        subGroup: String = "",
        type: GroupType,
        projectId: String
    ): Mono<String>
    fun getById(id: String): Mono<TestGroup>
    fun getGroups(projectId: String, type: String): Flux<TestGroup>
    fun getGroups(projectId: String): Flux<TestGroup>
    fun getGroupsByExecutionId(projectId: String, executionId: String): Flux<TestGroup>
}