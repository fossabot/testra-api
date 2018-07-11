package tech.testra.reportal.api.rest.handlers

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.core.annotation.Order
import org.springframework.core.codec.DecodingException
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.CONFLICT
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebExceptionHandler
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono
import tech.testra.reportal.api.rest.extensions.toErrorResponse
import tech.testra.reportal.exception.InvalidGroupException
import tech.testra.reportal.exception.ProjectAlreadyExistsException
import tech.testra.reportal.exception.ProjectNotFoundException
import tech.testra.reportal.exception.TestCaseAlreadyExistsException
import tech.testra.reportal.exception.TestCaseNotFoundException
import tech.testra.reportal.exception.TestExecutionNotFoundException
import tech.testra.reportal.exception.TestResultNotFoundException
import tech.testra.reportal.exception.TestScenarioAlreadyExistsException
import tech.testra.reportal.exception.TestScenarioNotFoundException

@Component
@Order(-2)
class ErrorHandler(private val objectMapper: ObjectMapper) : WebExceptionHandler {

    companion object {
        private val log = LoggerFactory.getLogger(ErrorHandler::class.java)
    }

    override fun handle(exchange: ServerWebExchange?, ex: Throwable?): Mono<Void> {
        log.error("Error " + ex.toString())
        val serverHttpResponse = exchange!!.response

        when (ex!!) {
            is ProjectNotFoundException -> serverHttpResponse.statusCode = NOT_FOUND
            is ProjectAlreadyExistsException -> serverHttpResponse.statusCode = CONFLICT
            is TestScenarioNotFoundException -> serverHttpResponse.statusCode = NOT_FOUND
            is TestScenarioAlreadyExistsException -> serverHttpResponse.statusCode = CONFLICT
            is TestCaseNotFoundException -> serverHttpResponse.statusCode = NOT_FOUND
            is TestCaseAlreadyExistsException -> serverHttpResponse.statusCode = CONFLICT
            is TestExecutionNotFoundException -> serverHttpResponse.statusCode = NOT_FOUND
            is TestResultNotFoundException -> serverHttpResponse.statusCode = NOT_FOUND
            is DecodingException -> serverHttpResponse.statusCode = BAD_REQUEST
            is InvalidGroupException -> serverHttpResponse.statusCode = BAD_REQUEST
            else -> {
                if (ex is ResponseStatusException)
                    serverHttpResponse.statusCode = ex.status
                else
                    serverHttpResponse.statusCode = INTERNAL_SERVER_ERROR
            }
        }

        val buffer = serverHttpResponse.bufferFactory()
            .wrap(objectMapper.writeValueAsBytes(ex.toErrorResponse()))

        serverHttpResponse.headers.contentType = MediaType.APPLICATION_JSON_UTF8
        return serverHttpResponse.writeWith(buffer.toMono())
    }
}