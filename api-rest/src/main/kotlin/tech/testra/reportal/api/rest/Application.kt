package tech.testra.reportal.api.rest

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.Banner
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.config.EnableWebFlux
import tech.testra.reportal.repository.ISimulationRepository
import tech.testra.reportal.repository.ITestCaseRepository
import tech.testra.reportal.repository.ITestExecutionRepository
import tech.testra.reportal.repository.ITestProjectRepository
import tech.testra.reportal.repository.ITestResultRepository
import tech.testra.reportal.repository.ITestScenarioRepository

@SpringBootApplication
@EnableWebFlux
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args) {
        setBannerMode(Banner.Mode.OFF)
    }
}

@Bean
fun init(scenarioRepository: ITestScenarioRepository): CommandLineRunner {
    return CommandLineRunner {
        scenarioRepository.findAll().subscribe()
    }
}

@Configuration
class WarmUpRepositories : CommandLineRunner {

    @Autowired
    lateinit var projectRepository: ITestProjectRepository

    @Autowired
    lateinit var scenarioRepository: ITestScenarioRepository

    @Autowired
    lateinit var executionRepository: ITestExecutionRepository

    @Autowired
    lateinit var resultRepository: ITestResultRepository

    @Autowired
    lateinit var testCaseRepository: ITestCaseRepository

    @Autowired
    lateinit var simulationRepository: ISimulationRepository

    override fun run(vararg args: String?) {
        projectRepository.count()
            .then(scenarioRepository.count())
            .then(testCaseRepository.count())
            .then(executionRepository.count())
            .then(resultRepository.count())
            .then(simulationRepository.count())
            .subscribe()
    }
}
