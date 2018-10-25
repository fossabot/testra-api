package tech.testra.reportal.api.rest

import org.apache.commons.csv.CSVFormat
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.Banner
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.web.reactive.config.EnableWebFlux
import reactor.core.publisher.toMono
import tech.testra.reportal.domain.entity.Vulnerability
import tech.testra.reportal.domain.entity.VulnerabilityCategory
import tech.testra.reportal.repository.IScanResultRepository
import tech.testra.reportal.repository.ISimulationRepository
import tech.testra.reportal.repository.ITestCaseRepository
import tech.testra.reportal.repository.ITestExecutionRepository
import tech.testra.reportal.repository.ITestProjectRepository
import tech.testra.reportal.repository.ITestResultRepository
import tech.testra.reportal.repository.ITestScenarioRepository
import tech.testra.reportal.repository.IVulnerabilityCategoryRepository
import tech.testra.reportal.repository.IVulnerabilityRepository
import java.io.InputStreamReader

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

    @Autowired
    lateinit var securityScanRepository: IScanResultRepository

    override fun run(vararg args: String?) {
        projectRepository.count()
            .then(scenarioRepository.count())
            .then(testCaseRepository.count())
            .then(executionRepository.count())
            .then(resultRepository.count())
            .then(simulationRepository.count())
            .then(securityScanRepository.count())
            .subscribe()
    }
}

@Configuration
class InitialiseVulnerabilitiesDataStore : CommandLineRunner {

    @Autowired
    lateinit var vulnerabilityRepository: IVulnerabilityRepository

    @Autowired
    lateinit var vulnerabilityCategoryRepository: IVulnerabilityCategoryRepository

    override fun run(vararg args: String?) {
        val catagoriesFile = ClassPathResource("vulnerability-categories.csv").inputStream
        val categoriesFileReader = InputStreamReader(catagoriesFile)
        val categories = CSVFormat.EXCEL.parse(categoriesFileReader)

        categories.forEach {
            val vulnerabilityCategory = VulnerabilityCategory(
                name = it[0]
            )

            vulnerabilityCategoryRepository.save(vulnerabilityCategory.toMono()).block()
        }

        val vulnerabilitiesFile = ClassPathResource("vulnerabilities.csv").inputStream
        val vulnerabilitiesFileReader = InputStreamReader(vulnerabilitiesFile)
        val vulnerabilities = CSVFormat.EXCEL.parse(vulnerabilitiesFileReader)

        val categoriesInStore = vulnerabilityCategoryRepository.findAll().collectList().block()

        vulnerabilities.forEach {
            val vulnerability = it
            val vulCat = categoriesInStore.find { it.name == vulnerability[1] }

            if (vulCat != null) {
                vulnerabilityRepository.save(Vulnerability(
                    name = vulnerability[0],
                    vulnerabilityCatId = vulCat.id
                ).toMono()).subscribe()
            }
        }
    }
}
