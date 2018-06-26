package tech.testra.reportal.api.rest

import org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric
import org.junit.Before
import org.junit.Test
import org.springframework.http.MediaType.APPLICATION_JSON_UTF8
import org.springframework.web.reactive.function.BodyInserters
import tech.testra.reportal.api.rest.response.ErrorResponse
import tech.testra.reportal.domain.entity.Project

class TestProjectIntegrationTest : BaseIntegrationTest() {

    lateinit var projectName: String

    val projectDescription = "Project Description"

    @Before
    fun setup() {
        projectName = randomAlphanumeric(10)
    }

    @Test
    fun testCreateProjectReturnCreated() {
        webTestClient.post().uri(PROJECT_ENDPOINT)
            .contentType(APPLICATION_JSON_UTF8)
            .body(BodyInserters.fromObject("""{ "name": "$projectName", "description": "$projectDescription" } """))
            .exchange()
            .expectStatus().isCreated
            .expectHeader().contentType(APPLICATION_JSON_UTF8)
            .expectBody()
            .jsonPath(".name").isEqualTo(projectName)
    }

    @Test
    fun testCreateExistingProjectReturnsProjectAlreadyExists() {
        testCreateProjectReturnCreated()

        webTestClient.post().uri(PROJECT_ENDPOINT)
            .contentType(APPLICATION_JSON_UTF8)
            .body(BodyInserters.fromObject("""{ "name": "$projectName", "description": "$projectDescription" } """))
            .exchange()
            .expectStatus().is4xxClientError
            .expectHeader().contentType(APPLICATION_JSON_UTF8)
    }

    @Test
    fun testCreateProjectWithInvalidPayloadReturnsBadRequest() {
        webTestClient.post().uri(PROJECT_ENDPOINT)
            .contentType(APPLICATION_JSON_UTF8)
            .body(BodyInserters.fromObject("""{ "INVALID_KEY": "$projectName" } """))
            .exchange()
            .expectStatus().isBadRequest
            .expectHeader().contentType(APPLICATION_JSON_UTF8)
    }

    @Test
    fun testUpdateProjectReturnsOk() {
        val addedProject = webTestClient.post().uri(PROJECT_ENDPOINT)
            .contentType(APPLICATION_JSON_UTF8)
            .body(BodyInserters.fromObject("""{ "name": "$projectName", "description": "$projectDescription" } """))
            .exchange()
            .expectStatus().isCreated
            .expectHeader().contentType(APPLICATION_JSON_UTF8)
            .returnResult(Project::class.java)
            .responseBody
            .blockFirst()

        val newProjectName = "Updated_$projectName"

        webTestClient.put().uri("$PROJECT_ENDPOINT${addedProject.id}")
            .contentType(APPLICATION_JSON_UTF8)
            .body(BodyInserters.fromObject("""{ "name": "$newProjectName", "description": "$projectDescription" } """))
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType(APPLICATION_JSON_UTF8)
            .expectBody()
            .jsonPath(".id").isEqualTo(addedProject.id)
            .jsonPath(".name").isEqualTo(newProjectName)
    }

    @Test
    fun testUpdateProjectWithInvalidProjectIdReturnsProjectNotFound() {
        webTestClient.put().uri("${PROJECT_ENDPOINT}1234567890")
            .contentType(APPLICATION_JSON_UTF8)
            .body(BodyInserters.fromObject("""{ "name": "Test Project", "description": "$projectDescription" } """))
            .exchange()
            .expectStatus().isNotFound
            .expectHeader().contentType(APPLICATION_JSON_UTF8)
            .expectBody(ErrorResponse::class.java)
    }

    @Test
    fun testGetProjectById() {
        val addedProject = webTestClient.post().uri(PROJECT_ENDPOINT)
            .contentType(APPLICATION_JSON_UTF8)
            .body(BodyInserters.fromObject("""{ "name": "$projectName", "description": "$projectDescription" } """))
            .exchange()
            .expectStatus().isCreated
            .expectHeader().contentType(APPLICATION_JSON_UTF8)
            .returnResult(Project::class.java)
            .responseBody
            .blockFirst()

        webTestClient.get().uri("$PROJECT_ENDPOINT${addedProject.id}")
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType(APPLICATION_JSON_UTF8)
            .expectBody()
            .jsonPath(".id").isEqualTo(addedProject.id)
            .jsonPath(".name").isEqualTo(projectName)
    }

    @Test
    fun testGetProjectByWithInvalidIdReturnsProjectNotFound() {
        webTestClient.get().uri("${PROJECT_ENDPOINT}1234567890")
            .exchange()
            .expectStatus().isNotFound
            .expectHeader().contentType(APPLICATION_JSON_UTF8)
            .expectBody(ErrorResponse::class.java)
    }

    @Test
    fun testGetProjectsReturnsEmptyArray() {
        webTestClient.get().uri(PROJECT_ENDPOINT)
            .accept(APPLICATION_JSON_UTF8)
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType(APPLICATION_JSON_UTF8)
            .expectBodyList(Project::class.java)
    }
}