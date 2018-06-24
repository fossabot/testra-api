package tech.testra.reportal.domain.entity

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.IndexDirection
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import tech.testra.reportal.domain.valueobjects.Attachment
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
    @Id @Indexed(direction = IndexDirection.DESCENDING) override val id: String = generatedUniqueId(),
    val name: String
) : IEntity

@Document(collection = "testcases")
@CompoundIndex(def = "{'projectId': 1, 'namespaceId': 1, 'name': 1}",
    name = "compound_index_project_namespace_testcase", unique = true)
data class TestCase(
    @Id override val id: String = generatedUniqueId(),
    val projectId: String,
    val name: String,
    val namespaceId: String
) : IEntity

@Document(collection = "executions")
data class TestExecution(
    @Id override val id: String = ObjectId().toString(),
    @Indexed(direction = IndexDirection.DESCENDING) val projectId: String,
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long?,
    val host: String,
    val isParallel: Boolean,
    val environment: String,
    val branch: String,
    val tags: List<String>
) : IEntity

@Document(collection = "results")
@CompoundIndex(def = "{'projectId': 1, 'executionId': 1}",
    name = "compound_index_project_execution",
    direction = IndexDirection.DESCENDING)
data class TestResult(
    @Id override var id: String = generatedUniqueId(),
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
    val attachments: List<Attachment>,
    val stepResults: List<TestStepResult>
) : IEntity

@Document(collection = "scenarios")
@CompoundIndex(def = "{'projectId': 1, 'featureId': 1, 'name': 1}",
    name = "compound_index_project_featureId_name",
    direction = IndexDirection.DESCENDING)
data class TestScenario(
    @Id override val id: String = generatedUniqueId(),
    @Indexed(direction = IndexDirection.DESCENDING) val projectId: String,
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
    name = "compound_index_project_name",
    direction = IndexDirection.DESCENDING,
    unique = true)
data class TestGroup(
    @Id override val id: String = generatedUniqueId(),
    val projectId: String,
    val name: String,
    val type: GroupType,
    val description: String = "",
    val subGroup: String = ""
) : IEntity

private fun generatedUniqueId() = ObjectId().toString()