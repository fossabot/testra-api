package tech.testra.reportal.service.interfaces

import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import tech.testra.reportal.domain.entity.Project
import tech.testra.reportal.domain.entity.TestCase
import tech.testra.reportal.domain.entity.TestExecution
import tech.testra.reportal.domain.entity.TestResult
import tech.testra.reportal.domain.entity.TestScenario
import tech.testra.reportal.model.ProjectModel
import tech.testra.reportal.model.TestCaseModel
import tech.testra.reportal.model.TestExecutionModel
import tech.testra.reportal.model.TestResultModel
import tech.testra.reportal.model.TestScenarioModel

@Service
interface ITestProjectService {
    fun getProjects(): Flux<Project>
    fun getProject(idOrName: String): Mono<Project>
    fun createProject(projectModelMono: Mono<ProjectModel>): Mono<Project>
    fun updateProject(id: String, projectModelMono: Mono<ProjectModel>): Mono<Project>
    fun deleteProjectById(id: String): Mono<Boolean>
    fun getSize(): Long
}

@Service
interface ITestScenarioService {
    fun getScenariosByProjectId(projectId: String): Flux<TestScenario>
    fun getScenarioById(projectId: String, scenarioId: String): Mono<TestScenario>
    fun createScenario(projectId: String, testScenarioModelMono: Mono<TestScenarioModel>): Mono<TestScenario>
    fun updateScenario(
        projectId: String,
        scenarioId: String,
        testScenarioModelMono: Mono<TestScenarioModel>
    ): Mono<TestScenario>

    fun deleteScenarioById(id: String): Mono<Boolean>
    fun getSize(): Long
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

    fun deleteTestCaseById(id: String): Mono<Boolean>
    fun getSize(): Long
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
    fun deleteExecutionById(id: String): Mono<Boolean>
    fun getSize(): Long
}

@Service
interface ITestResultService {
    fun getResults(projectId: String, executionId: String): Flux<TestResult>
    fun getResults(projectId: String, executionId: String, result: String): Flux<TestResult>
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

    fun deleteResultById(id: String): Mono<Boolean>
    fun getSize(): Long
}

interface ITestGroupService {
    fun getOrAddGroup(groupName: String, groupDescription: String = "", projectId: String): Mono<String>
}