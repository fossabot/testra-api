package tech.testra.reportal.domain.entity

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import tech.testra.reportal.domain.valueobjects.AttachmentVO
import tech.testra.reportal.domain.valueobjects.GroupType
import tech.testra.reportal.domain.valueobjects.Result
import tech.testra.reportal.domain.valueobjects.ResultType
import tech.testra.reportal.domain.valueobjects.TestStep
import tech.testra.reportal.domain.valueobjects.TestStepResult

interface IEntity {
    val id: String
}

@Document(collection = "projects")
@CompoundIndex(def = "{'name': 1}",
    useGeneratedName = true, unique = true)
data class Project(
    @Id @Indexed override val id: String = generatedUniqueId(),
    val name: String
) : IEntity

@Document(collection = "testcases")
@CompoundIndex(def = "{'projectId': 1, 'namespaceId': 1, 'name': 1}",
    name = "compound_index_project_namespace_testcase", unique = true)
data class TestCase(
    @Id @Indexed override val id: String = generatedUniqueId(),
    val projectId: String,
    val name: String,
    val namespaceId: String
) : IEntity

@Document(collection = "executions")
data class TestExecution(
    @Id override val id: String = ObjectId().toString(),
    @Indexed val projectId: String,
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long?,
    val host: String?,
    val isParallel: Boolean
) : IEntity

@Document(collection = "results")
@CompoundIndex(def = "{'projectId': 1, 'executionId': 1}", name = "compound_index_project_execution")
data class TestResult(
    @Id override val id: String = generatedUniqueId(),
    val projectId: String,
    val executionId: String,
    val targetId: String,
    val resultType: ResultType,
    val result: Result,
    val error: String = "",
    val durationInMs: Long,
    val startTime: Long,
    val endTime: Long,
    val retryCount: Long = 0,
    val attachments: List<AttachmentVO>,
    val stepResults: List<TestStepResult>
) : IEntity

@Document(collection = "scenarios")
data class TestScenario(
    @Id @Indexed override val id: String = generatedUniqueId(),
    @Indexed val projectId: String,
    val featureId: String,
    val featureDescription: String,
    val name: String,
    val tags: List<String> = emptyList(),
    val before: List<TestStep> = emptyList(),
    val after: List<TestStep> = emptyList(),
    val backgroundSteps: List<TestStep> = emptyList(),
    val steps: List<TestStep>
) : IEntity

@Document(collection = "groups")
@CompoundIndex(def = "{'projectId': 1, 'name': 1}",
    name = "compound_index_project_feature", unique = true)
data class TestGroup(
    @Id override val id: String = generatedUniqueId(),
    val projectId: String,
    val name: String,
    val type: GroupType,
    val description: String = ""
) : IEntity

@Document(collection = "attachments")
data class Attachment(
    @Id @Indexed override val id: String = generatedUniqueId(),
    val name: String,
    val base64EncodedByteArray: String
) : IEntity

private fun generatedUniqueId() = ObjectId().toString()