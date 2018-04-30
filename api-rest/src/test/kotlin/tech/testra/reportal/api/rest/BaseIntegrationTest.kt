package tech.testra.reportal.api.rest

import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.web.reactive.server.WebTestClient

@RunWith(SpringJUnit4ClassRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
abstract class BaseIntegrationTest {

    val PROJECT_ENDPOINT = Router.ENDPOINT_PREFIX + "/projects/"

    @Autowired
    lateinit var webTestClient: WebTestClient

    @Test
    fun contextLoads() {
    }
}