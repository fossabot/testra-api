package tech.testra.reportal.service.result

import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono
import tech.testra.reportal.domain.entity.TestResult
import tech.testra.reportal.domain.valueobjects.Result
import tech.testra.reportal.domain.valueobjects.ResultType
import tech.testra.reportal.extension.flatMapManyWithResumeOnError
import tech.testra.reportal.extension.flatMapWithResumeOnError
import tech.testra.reportal.extension.toAttachmentDomain
import tech.testra.reportal.extension.toTestStepResultDomain
import tech.testra.reportal.model.TestResultModel
import tech.testra.reportal.repository.ITestExecutionStatsRepository
import tech.testra.reportal.repository.ITestResultRepository
import tech.testra.reportal.service.interfaces.ITestCaseService
import tech.testra.reportal.service.interfaces.ITestExecutionService
import tech.testra.reportal.service.interfaces.ITestResultService
import tech.testra.reportal.service.interfaces.ITestScenarioService
import tech.testra.reportal.model.Result as ResultInModel

@Service
class TestResultService(
    val _testResultRepository: ITestResultRepository,
    val _testExecutionService: ITestExecutionService,
    val _testScenarioService: ITestScenarioService,
    val _testCaseService: ITestCaseService,
    val _testExecutionStatsRepository: ITestExecutionStatsRepository
) : ITestResultService {

    override fun getResults(projectId: String, executionId: String): Flux<TestResult> =
        _testExecutionService.getExecutionById(projectId, executionId)
            .flatMapManyWithResumeOnError {
                _testResultRepository.findAll(projectId, it.id)
            }

    override fun getResults(projectId: String, executionId: String, result: String): Flux<TestResult> =
        _testExecutionService.getExecutionById(projectId, executionId)
            .flatMapManyWithResumeOnError {
                _testResultRepository.findAll(projectId, it.id, Result.valueOf(result))
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

    override fun getCount(): Long =
        _testResultRepository.size().blockOptional()
            .map { it }
            .orElse(-1L)

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
                        result = Result.valueOf(it.result.name),
                        error = it.error,
                        durationInMs = it.durationInMs,
                        startTime = it.startTime,
                        endTime = it.endTime,
                        retryCount = it.retryCount,
                        stepResults = it.stepResults.toTestStepResultDomain(),
                        attachments = it.attachments.toAttachmentDomain()
                    )
                    if (previousTestResult != null) testResult.id = previousTestResult.id
                    updateTestExecution(executionId, it)
                    updateTestExecutionStats(executionId, previousTestResult, it)
                    _testResultRepository.save(testResult.toMono())
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
            if (previousTestResult.result.toString() == testResultModel.result.toString())
                return

            incTestExecutionStats(testResultModel, executionId)

            when (previousTestResult.result) {
                Result.PASSED -> _testExecutionStatsRepository.decPassedResults(executionId).subscribe()
                Result.FAILED -> _testExecutionStatsRepository.decFailedResults(executionId).subscribe()
                else -> _testExecutionStatsRepository.decOtherResults(executionId).subscribe()
            }
        }
    }

    private fun incTestExecutionStats(testResultModel: TestResultModel, executionId: String) {
        when (testResultModel.result) {
            ResultInModel.PASSED -> _testExecutionStatsRepository.incPassedResults(executionId).subscribe()
            ResultInModel.FAILED -> _testExecutionStatsRepository.incFailedResults(executionId).subscribe()
            else -> _testExecutionStatsRepository.incOtherResults(executionId).subscribe()
        }
    }

    private fun validatedTestResultModel(trm: TestResultModel, projectId: String): Mono<TestResultModel> =
        when (ResultType.valueOf(trm.resultType.toString())) {
            ResultType.TEST_CASE -> _testCaseService.getTestCaseById(projectId, trm.targetId)
                .flatMapWithResumeOnError { trm.toMono() }
            ResultType.SCENARIO -> _testScenarioService.getScenarioById(projectId, trm.targetId)
                .flatMapWithResumeOnError { trm.toMono() }
            else -> trm.toMono()
        }
}
