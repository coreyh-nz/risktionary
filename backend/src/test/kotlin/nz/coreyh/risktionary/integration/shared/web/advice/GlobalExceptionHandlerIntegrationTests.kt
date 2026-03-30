package nz.coreyh.risktionary.integration.shared.web.advice

import com.ninjasquad.springmockk.MockkBean
import nz.coreyh.risktionary.auth.application.service.AuthTokenService
import nz.coreyh.risktionary.auth.config.AuthConfiguration
import nz.coreyh.risktionary.integration.shared.web.advice.GlobalExceptionHandlerTest.Companion.TEST_APP_EXCEPTION_ROUTE
import nz.coreyh.risktionary.integration.shared.web.advice.GlobalExceptionHandlerTest.Companion.TEST_UNKNOWN_EXCEPTION_ROUTE
import nz.coreyh.risktionary.shared.exception.AppException
import nz.coreyh.risktionary.shared.exception.code.ErrorCode
import nz.coreyh.risktionary.shared.web.advice.GlobalExceptionHandler
import org.junit.jupiter.api.Test
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.TestConstructor
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class GlobalExceptionHandlerTestController {
    @GetMapping(TEST_APP_EXCEPTION_ROUTE)
    fun throwAppException(): String = throw object : AppException(ErrorCode.INVALID_REQUEST) {}

    @GetMapping(TEST_UNKNOWN_EXCEPTION_ROUTE)
    fun throwUnknownException(): String = throw RuntimeException()
}

@WebMvcTest(controllers = [GlobalExceptionHandlerTestController::class])
@Import(GlobalExceptionHandler::class)
@AutoConfigureMockMvc(addFilters = false)
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class GlobalExceptionHandlerTest(
    private val mockMvc: MockMvc,
) {
    companion object {
        const val TEST_APP_EXCEPTION_ROUTE = "/test/app-exception"
        const val TEST_UNKNOWN_EXCEPTION_ROUTE = "/test/unknown-exception"
    }

    @MockkBean
    private lateinit var authTokenService: AuthTokenService

    @MockkBean
    private lateinit var authConfiguration: AuthConfiguration

    @Test
    fun `AppException should return mapped error response`() {
        mockMvc
            .perform(get(TEST_APP_EXCEPTION_ROUTE))
            .andExpect {
                status().isBadRequest
                jsonPath("$.errorCode").value(ErrorCode.INVALID_REQUEST.code)
                jsonPath("$.errorCode").value(ErrorCode.INVALID_REQUEST.defaultMessage)
            }
    }

    @Test
    fun `unknown exception should return internal server error`() {
        mockMvc
            .perform(get(TEST_UNKNOWN_EXCEPTION_ROUTE))
            .andExpect {
                status().isInternalServerError
                jsonPath("$.errorCode").value(ErrorCode.INTERNAL_ERROR.code)
                jsonPath("$.errorCode").value(ErrorCode.INTERNAL_ERROR.defaultMessage)
            }
    }

    @Test
    fun `unknown route should return mapped 404 response`() {
        mockMvc
            .perform(get("/test/does-not-exist"))
            .andExpect {
                status().isNotFound
                jsonPath("$.errorCode").value(ErrorCode.NOT_FOUND.code)
                jsonPath("$.errorCode").value(ErrorCode.NOT_FOUND.defaultMessage)
            }
    }
}
