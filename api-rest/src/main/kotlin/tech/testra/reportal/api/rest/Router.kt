package tech.testra.reportal.api.rest

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.router
import tech.testra.reportal.api.rest.handlers.CounterHandler
import tech.testra.reportal.api.rest.handlers.TestCaseHandler
import tech.testra.reportal.api.rest.handlers.TestExecutionHandler
import tech.testra.reportal.api.rest.handlers.TestGroupHandler
import tech.testra.reportal.api.rest.handlers.TestProjectHandler
import tech.testra.reportal.api.rest.handlers.TestResultHandler
import tech.testra.reportal.api.rest.handlers.TestScenarioHandler

@Configuration
class Router(
    private val _testExecutionHandler: TestExecutionHandler,
    private val _testProjectHandler: TestProjectHandler,
    private val _testScenarioHandler: TestScenarioHandler,
    private val _testCaseHandler: TestCaseHandler,
    private val _testResultHandler: TestResultHandler,
    private val _testGroupHandler: TestGroupHandler,
    private val _counterHandler: CounterHandler
) {

    companion object {
        const val ENDPOINT_PREFIX = "/api/v1/"
        const val PROJECT_ID_IN_RESOURCE = "{projectId}"
        const val SCENARIO_ID_IN_RESOURCE = "{scenarioId}"
        const val TESTCASE_ID_IN_RESOURCE = "{testCaseId}"
        const val EXECUTION_ID_IN_RESOURCE = "{executionId}"
        const val RESULT_ID_IN_RESOURCE = "{resultId}"
    }

    @Bean
    fun apiRouter() = router {
        accept(MediaType.APPLICATION_JSON_UTF8).nest {
            ENDPOINT_PREFIX.nest {
                "/projects".nest {
                    GET("/", _testProjectHandler::getAllProjects)
                    GET("/execution-filters", _testProjectHandler::executionFilters)
                    POST("/", _testProjectHandler::createProject)
                    GET("/$PROJECT_ID_IN_RESOURCE", _testProjectHandler::getProjectById)
                    PUT("/$PROJECT_ID_IN_RESOURCE", _testProjectHandler::updateProject)
                    DELETE("/$PROJECT_ID_IN_RESOURCE", _testProjectHandler::deleteProject)

                    "/{projectId}/scenarios".nest {
                        GET("/", _testScenarioHandler::findAll)
                        POST("/", _testScenarioHandler::create)
                        GET("/$SCENARIO_ID_IN_RESOURCE", _testScenarioHandler::findById)
                        PUT("/$SCENARIO_ID_IN_RESOURCE", _testScenarioHandler::update)
                        DELETE("/$SCENARIO_ID_IN_RESOURCE", _testScenarioHandler::delete)
                    }

                    "/{projectId}/testcases".nest {
                        GET("/", _testCaseHandler::findAll)
                        POST("/", _testCaseHandler::create)
                        GET("/$TESTCASE_ID_IN_RESOURCE", _testCaseHandler::findById)
                        PUT("/$TESTCASE_ID_IN_RESOURCE", _testCaseHandler::update)
                        DELETE("/$TESTCASE_ID_IN_RESOURCE", _testCaseHandler::delete)
                    }

                    "/{projectId}/executions".nest {
                        GET("/", _testExecutionHandler::findAll)
                        POST("/", _testExecutionHandler::createExecution)
                        GET("/$EXECUTION_ID_IN_RESOURCE", _testExecutionHandler::findById)
                        PUT("/$EXECUTION_ID_IN_RESOURCE", _testExecutionHandler::updateExecution)
                        DELETE("/$EXECUTION_ID_IN_RESOURCE", _testExecutionHandler::delete)

                        "/{executionId}/results".nest {
                            GET("/", _testResultHandler::findAll)
                            POST("/", _testResultHandler::create)
                            GET("/$RESULT_ID_IN_RESOURCE", _testResultHandler::findById)
                            PUT("/$RESULT_ID_IN_RESOURCE", _testResultHandler::update)
                            DELETE("/$RESULT_ID_IN_RESOURCE", _testResultHandler::delete)
                        }

                        "/{executionId}/groups".nest {
                            GET("/", _testGroupHandler::findAllByExecId)
                        }

                        "/{executionId}/result-stats".nest {
                            GET("/", _testExecutionHandler::resultStats)
                        }
                    }

                    "/{projectId}/test-groups".nest {
                        GET("/", _testGroupHandler::findAll)
                    }
                }
                "/counters".nest {
                    GET("/", _counterHandler::get)
                }
            }
        }
    }
}