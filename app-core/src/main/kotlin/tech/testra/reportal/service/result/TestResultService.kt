package tech.testra.reportal.service.result

import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.toFlux
import reactor.core.publisher.toMono
import tech.testra.reportal.domain.entity.TestResult
import tech.testra.reportal.domain.valueobjects.ResultStatus
import tech.testra.reportal.domain.valueobjects.ResultType
import tech.testra.reportal.exception.InvalidGroupException
import tech.testra.reportal.exception.TestResultNotFoundException
import tech.testra.reportal.extension.flatMapManyWithResumeOnError
import tech.testra.reportal.extension.flatMapWithResumeOnError
import tech.testra.reportal.extension.toAttachmentDomain
import tech.testra.reportal.extension.toEnrichedTestResult
import tech.testra.reportal.extension.toTestStepResultDomain
import tech.testra.reportal.model.EnrichedTestResultModel
import tech.testra.reportal.model.TestResultModel
import tech.testra.reportal.repository.ITestExecutionStatsRepository
import tech.testra.reportal.repository.ITestResultRepository
import tech.testra.reportal.service.interfaces.ITestCaseService
import tech.testra.reportal.service.interfaces.ITestExecutionService
import tech.testra.reportal.service.interfaces.ITestGroupService
import tech.testra.reportal.service.interfaces.ITestResultService
import tech.testra.reportal.service.interfaces.ITestScenarioService
import tech.testra.reportal.model.ResultStatus as ResultInModel

@Service
class TestResultService(
    private val _testResultRepository: ITestResultRepository,
    private val _testExecutionService: ITestExecutionService,
    private val _testScenarioService: ITestScenarioService,
    private val _testCaseService: ITestCaseService,
    private val _testGroupService: ITestGroupService,
    private val _testExecutionStatsRepository: ITestExecutionStatsRepository
) : ITestResultService {
    override fun getResults(projectId: String, executionId: String): Flux<EnrichedTestResultModel> =
        _testExecutionService.getExecutionById(projectId, executionId)
            .flatMapManyWithResumeOnError {
                _testResultRepository.findAll(projectId, it.id)
                    .switchIfEmpty(TestResultNotFoundException("").toFlux())
                    .flatMap { toEnrichedTestResultModel(projectId, it) }
            }

    override fun getResults(projectId: String, executionId: String, status: ResultInModel): Flux<EnrichedTestResultModel> =
        _testExecutionService.getExecutionById(projectId, executionId)
            .flatMapManyWithResumeOnError {
                _testResultRepository.findAll(projectId, it.id, ResultStatus.valueOf(status.name))
                    .flatMap { toEnrichedTestResultModel(projectId, it) }
            }

    override fun getResults(projectId: String, executionId: String, groupId: String): Flux<EnrichedTestResultModel> =
        _testExecutionService.getExecutionById(projectId, executionId)
            .flatMapManyWithResumeOnError {
                _testResultRepository.findAll(projectId, it.id, groupId)
                    .flatMap { toEnrichedTestResultModel(projectId, it) }
            }

    override fun getResultById(projectId: String, executionId: String, resultId: String): Mono<TestResult> =
        _testExecutionService.getExecutionById(projectId, executionId)
            .flatMapWithResumeOnError { _testResultRepository.findById(resultId) }

    override fun createResult(
        projectId: String,
        executionId: String,
        testResultModelMono: Mono<TestResultModel>
    ): Mono<TestResult> {
        return _testExecutionService.getExecutionById(projectId, executionId)
            .flatMapWithResumeOnError {
                createOrUpdate(testResultModelMono, projectId, executionId, null)
            }
    }

    override fun updateResult(
        projectId: String,
        executionId: String,
        resultId: String,
        testResultModelMono: Mono<TestResultModel>
    ): Mono<TestResult> {

        return this.getResultById(projectId, executionId, resultId)
            .flatMapWithResumeOnError {
                createOrUpdate(testResultModelMono, projectId, executionId, it)
            }
    }

    override fun deleteResultById(id: String): Mono<Boolean> = _testResultRepository.deleteById(id)

    override fun count(): Mono<Long> = _testResultRepository.count()

    private fun createOrUpdate(
        testResultModelMono: Mono<TestResultModel>,
        projectId: String,
        executionId: String,
        previousTestResult: TestResult?
    ): Mono<TestResult> {
        return testResultModelMono.flatMap {

            validatedTestResultModel(it, projectId)
                .onErrorResume { it.toMono() }
                .flatMap {
                    val testResult = TestResult(
                        projectId = projectId,
                        executionId = executionId,
                        targetId = it.targetId,
                        groupId = it.groupId,
                        resultType = ResultType.valueOf(it.resultType.name),
                        status = ResultStatus.valueOf(it.status.name),
                        error = it.error,
                        durationInMs = it.durationInMs,
                        startTime = it.startTime,
                        endTime = it.endTime,
                        retryCount = it.retryCount,
                        expectedToFail = it.expectedToFail,
                        stepResults = it.stepResults.toTestStepResultDomain(),
                        attachments = it.attachments.toAttachmentDomain()
                    )
                    updateTestExecution(executionId, it)
                    updateTestExecutionStats(executionId, previousTestResult, it)
                    if (previousTestResult == null)
                        _testResultRepository.save(testResult.toMono())
                    else
                        _testResultRepository.save(testResult.copy(id = previousTestResult.id).toMono())
                }
        }
    }

    private fun updateTestExecution(executionId: String, it: TestResultModel) {
        _testExecutionService.updateEndTime(executionId, it.endTime)
        _testExecutionService.pushGroupId(executionId, it.groupId)
    }

    private fun updateTestExecutionStats(
        executionId: String,
        previousTestResult: TestResult?,
        testResultModel: TestResultModel
    ) {
        if (previousTestResult == null) {
            incTestExecutionStats(testResultModel, executionId)
        } else {
            if (previousTestResult.status.toString() == testResultModel.status.toString())
                return

            incTestExecutionStats(testResultModel, executionId)

            when (previousTestResult.status) {
                ResultStatus.PASSED -> _testExecutionStatsRepository.decPassedResults(executionId)
                ResultStatus.FAILED -> _testExecutionStatsRepository.decFailedResults(executionId)
                else -> _testExecutionStatsRepository.decOtherResults(executionId)
            }.subscribe()
        }
    }

    private fun incTestExecutionStats(testResultModel: TestResultModel, executionId: String) {
        when (testResultModel.status) {
            ResultInModel.PASSED -> _testExecutionStatsRepository.incPassedResults(executionId)
            ResultInModel.FAILED -> _testExecutionStatsRepository.incFailedResults(executionId)
            else -> _testExecutionStatsRepository.incOtherResults(executionId)
        }.subscribe()
    }

    private fun validatedTestResultModel(trm: TestResultModel, projectId: String): Mono<TestResultModel> =
        _testGroupService.getById(trm.groupId)
            .switchIfEmpty(InvalidGroupException(trm.groupId).toMono())
            .flatMap {
                when (ResultType.valueOf(trm.resultType.toString())) {
                    ResultType.TEST_CASE -> _testCaseService.getTestCaseById(projectId, trm.targetId)
                        .flatMapWithResumeOnError { trm.toMono() }
                    ResultType.SCENARIO -> _testScenarioService.getScenarioById(projectId, trm.targetId)
                        .flatMapWithResumeOnError { trm.toMono() }
                    else -> trm.toMono()
                }
            }

    private fun toEnrichedTestResultModel(projectId: String, testResult: TestResult): Mono<EnrichedTestResultModel> =
        _testGroupService.getById(testResult.groupId).flatMap {
            val testGroup = it
            if (testResult.resultType == ResultType.SCENARIO)
                _testScenarioService.getScenarioById(projectId, testResult.targetId)
                    .map { testResult.toEnrichedTestResult(it, testGroup) }
            else
                _testCaseService.getTestCaseById(projectId, testResult.targetId)
                    .map { testResult.toEnrichedTestResult(it, testGroup) }
        }
}
