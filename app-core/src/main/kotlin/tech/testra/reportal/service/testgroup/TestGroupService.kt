package tech.testra.reportal.service.testgroup

import org.springframework.dao.DuplicateKeyException
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono
import tech.testra.reportal.domain.entity.TestGroup
import tech.testra.reportal.domain.valueobjects.GroupType
import tech.testra.reportal.extension.flatMapManyWithResumeOnError
import tech.testra.reportal.repository.ITestGroupRepository
import tech.testra.reportal.service.interfaces.ITestExecutionService
import tech.testra.reportal.service.interfaces.ITestGroupService
import tech.testra.reportal.service.interfaces.ITestProjectService

@Service
class TestGroupService(
    private val _testGroupRepository: ITestGroupRepository,
    private val _testExecutionService: ITestExecutionService,
    private val _testProjectService: ITestProjectService
) : ITestGroupService {

    override fun getGroups(projectId: String, type: String): Flux<TestGroup> =
        _testProjectService.getProject(projectId)
            .flatMapManyWithResumeOnError { _testGroupRepository.findAll(projectId, GroupType.valueOf(type)) }

    override fun getGroups(projectId: String): Flux<TestGroup> =
        _testProjectService.getProject(projectId)
            .flatMapManyWithResumeOnError { _testGroupRepository.findAll(projectId) }

    override fun getGroupsByExecutionId(projectId: String, executionId: String): Flux<TestGroup> =
        _testExecutionService.getExecutionById(projectId, executionId)
            .flatMapIterable { it.groupIds }
            .flatMap { _testGroupRepository.findById(it) }

    override fun getById(id: String): Mono<TestGroup> = _testGroupRepository.findById(id)

    override fun getOrAddGroup(
        groupName: String,
        groupDescription: String,
        subGroup: String,
        type: GroupType,
        projectId: String
    ): Mono<String> {
        return _testGroupRepository.findBy(groupName, projectId)
            .map { it.id }
            .switchIfEmpty(
                _testGroupRepository
                    .save(TestGroup(projectId = projectId,
                        name = groupName,
                        description = groupDescription,
                        subGroup = subGroup,
                        type = type).toMono())
                    .map { it.id }
                    // Handles race condition
                    .onErrorResume(DuplicateKeyException::class.java) {
                        _testGroupRepository
                            .findBy(groupName, projectId)
                            .map { it.id }
                    }
            )
    }
}