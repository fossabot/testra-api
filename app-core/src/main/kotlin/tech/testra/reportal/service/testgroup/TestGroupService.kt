package tech.testra.reportal.service.testgroup

import org.springframework.dao.DuplicateKeyException
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono
import tech.testra.reportal.domain.entity.TestGroup
import tech.testra.reportal.domain.valueobjects.GroupType
import tech.testra.reportal.repository.ITestGroupRepository
import tech.testra.reportal.service.interfaces.ITestGroupService

@Service
class TestGroupService(val _testGroupRepository: ITestGroupRepository) : ITestGroupService {

    override fun getOrAddGroup(groupName: String, groupDescription: String, projectId: String): Mono<String> {
        return _testGroupRepository.findBy(groupName, projectId)
            .map { it.id }
            .switchIfEmpty(
                _testGroupRepository
                    .save(TestGroup(projectId = projectId,
                        name = groupName,
                        description = groupDescription,
                        type = GroupType.FEATURE).toMono())
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