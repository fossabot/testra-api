package tech.testra.reportal.api.rest

import org.springframework.boot.Banner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import tech.testra.reportal.repository.TestCaseRepository
import tech.testra.reportal.repository.TestExecutionRepository
import tech.testra.reportal.repository.TestGroupRepository
import tech.testra.reportal.repository.TestProjectRepository
import tech.testra.reportal.repository.TestResultRepository
import tech.testra.reportal.repository.TestScenarioRepository
import tech.testra.reportal.service.execution.TestExecutionService
import tech.testra.reportal.service.project.TestProjectService
import tech.testra.reportal.service.result.TestResultService
import tech.testra.reportal.service.scenario.TestScenarioService
import tech.testra.reportal.service.testcase.TestCaseService
import tech.testra.reportal.service.testgroup.TestGroupService

@SpringBootApplication
class Application {
    @Bean
    fun testProjectRepository() = TestProjectRepository()

    @Bean
    fun testCaseRepository() = TestCaseRepository()

    @Bean
    fun testExecutionRepository() = TestExecutionRepository()

    @Bean
    fun testScenarioRepository() = TestScenarioRepository()

    @Bean
    fun testResultRepository() = TestResultRepository()

    @Bean
    fun testGroupRepository() = TestGroupRepository()

    @Bean
    fun testProjectService() = TestProjectService(testProjectRepository())

    @Bean
    fun testGroupService() = TestGroupService(testGroupRepository())

    @Bean
    fun testScenarioService() =
        TestScenarioService(testScenarioRepository(), testProjectService(), testGroupService())

    @Bean
    fun testCaseService() =
        TestCaseService(testCaseRepository(), testProjectService(), testGroupService())

    @Bean
    fun testExecutionService() =
        TestExecutionService(testExecutionRepository(), testProjectService())

    @Bean
    fun testResultService() =
        TestResultService(testResultRepository(),
            testExecutionService(),
            testScenarioService(),
            testCaseService())
}

fun main(args: Array<String>) {
    runApplication<Application>(*args) {
        setBannerMode(Banner.Mode.OFF)
    }
}