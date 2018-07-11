package tech.testra.reportal.api.rest.configurations

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import tech.testra.reportal.repository.TestCaseRepository
import tech.testra.reportal.repository.TestExecutionRepository
import tech.testra.reportal.repository.TestExecutionStatsRepository
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

@Configuration
class BeanConfiguration {
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
    fun testExecutionStatsRepository() = TestExecutionStatsRepository()

    @Bean
    fun testProjectService() = TestProjectService(testProjectRepository(),
        testExecutionRepository(),
        testResultRepository(),
        testScenarioRepository(),
        testCaseRepository(),
        testGroupRepository(),
        testExecutionStatsRepository())

    @Bean
    fun testGroupService() =
        TestGroupService(testGroupRepository(), testExecutionService(), testProjectService())

    @Bean
    fun testScenarioService() =
        TestScenarioService(testScenarioRepository(), this.testProjectService(), testGroupService())

    @Bean
    fun testCaseService() =
        TestCaseService(testCaseRepository(), testProjectService(), testGroupService())

    @Bean
    fun testExecutionService() =
        TestExecutionService(testExecutionRepository(), testExecutionStatsRepository(), this.testProjectService())

    @Bean
    fun testResultService() =
        TestResultService(testResultRepository(),
            testExecutionService(),
            this.testScenarioService(),
            testCaseService(),
            testGroupService(),
            testExecutionStatsRepository())
}