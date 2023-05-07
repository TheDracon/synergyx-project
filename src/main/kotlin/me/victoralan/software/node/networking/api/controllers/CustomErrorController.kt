package me.victoralan.software.node.networking.api.controllers

import jakarta.servlet.http.HttpServletRequest
import me.victoralan.software.node.networking.api.ErrorResponse
import org.springframework.boot.web.servlet.error.ErrorController
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody


@Controller
class CustomErrorController : ErrorController {
    @RequestMapping("/error")
    @ResponseBody
    fun handleError(request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        val status = getStatus(request)
        val errorResponse = ErrorResponse(status.value(), "An error occurred")
        return ResponseEntity(errorResponse, status)
    }

    private fun getStatus(request: HttpServletRequest): HttpStatus {
        val statusCode = request.getAttribute("javax.servlet.error.status_code")
        if (statusCode != null) {
            try {
                return HttpStatus.valueOf(statusCode as Int)
            } catch (ex: Exception) {
                // Do nothing
            }
        }
        return HttpStatus.INTERNAL_SERVER_ERROR
    }
}