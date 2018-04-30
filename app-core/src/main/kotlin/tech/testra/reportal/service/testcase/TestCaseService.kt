package tech.testra.reportal.service.testcase

import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono
import tech.testra.reportal.domain.entity.TestCase
import tech.testra.reportal.exception.ProjectNotFoundException
import tech.testra.reportal.exception.TestCaseAlreadyExistsException
import tech.testra.reportal.exception.TestCaseNotFoundException
import tech.testra.reportal.extension.flatMapManyWithResumeOnError
import tech.testra.reportal.extension.flatMapWithResumeOnError
import tech.testra.reportal.extension.onDuplicateKeyException
import tech.testra.reportal.extension.orElseGetException
import tech.testra.reportal.model.TestCaseModel
import tech.testra.reportal.repository.ITestCaseRepository
import tech.testra.reportal.service.interfaces.ITestCaseService
import tech.testra.reportal.service.interfaces.ITestGroupService
import tech.testra.reportal.service.interfaces.ITestProjectService

@Service
class TestCaseService(
    val _testCaseRepository: ITestCaseRepository,
    val _testProjectService: ITestProjectService,
    val _testGroupService: ITestGroupService
) : ITestCaseService {

    override fun getTestCasesByProjectId(projectId: String): Flux<TestCase> =
        _testProjectService.getProjectById(projectId)
            .flatMapManyWithResumeOnError { _testCaseRepository.findAllByProjectId(it.id) }

    override fun getTestCaseById(projectId: String, testCaseId: String): Mono<TestCase> =
        _testProjectService.getProjectById(projectId)
            .flatMapWithResumeOnError {
                _testCaseRepository.findById(testCaseId)
                    .orElseGetException(TestCaseNotFoundException(testCaseId))
            }

    override fun createTestCase(projectId: String, testCaseModelMono: Mono<TestCaseModel>): Mono<TestCase> {
        return _testProjectService.getProjectById(projectId)
            .switchIfEmpty(ProjectNotFoundException(projectId).toMono())
            .flatMap {
                testCaseModelMono.flatMap {
                    val testCaseModel = it
                    // Get feature if exists otherwise create one
                    _testGroupService.getOrAddGroup(it.namespace, projectId)
                        .flatMap {

                            // If test case name already exists throw an exception
                            val testCaseName = testCaseModel.name
                            val testCase = TestCase(projectId = projectId,
                                name = testCaseName,
                                namespaceId = it)
                            _testCaseRepository.save(testCase.toMono())
                                .flatMap { it.toMono() }
                                .onDuplicateKeyException { TestCaseAlreadyExistsException(testCaseName) }
                        }
                }
            }
    }

    override fun updateTestCase(
        projectId: String,
        testCaseId: String,
        testCaseModelMono: Mono<TestCaseModel>
    ): Mono<TestCase> {
        return _testProjectService.getProjectById(projectId)
            .switchIfEmpty(ProjectNotFoundException(projectId).toMono())
            .flatMap {
                testCaseModelMono.flatMap {
                    val testCaseModel = it
                    _testCaseRepository.findById(testCaseId)
                        .flatMap {
                            // Get feature if exists otherwise create one
                            _testGroupService.getOrAddGroup(testCaseModel.namespace, projectId)
                                .flatMap {

                                    // If test case name already exists throw an exception
                                    val testCase = TestCase(id = testCaseId,
                                        projectId = projectId,
                                        name = testCaseModel.name,
                                        namespaceId = it)

                                    _testCaseRepository.save(testCase.toMono())
                                        .flatMap { it.toMono() }
                                        .onDuplicateKeyException { TestCaseAlreadyExistsException(testCaseModel.name) }
                                }
                        }
                        .switchIfEmpty(TestCaseNotFoundException(testCaseId).toMono())
                }
            }
    }

    override fun deleteTestCaseById(id: String): Mono<Boolean> = _testCaseRepository.deleteById(id)

    override fun getSize(): Long =
        _testCaseRepository.size().blockOptional()
            .map { it }
            .orElse(-1L)
}