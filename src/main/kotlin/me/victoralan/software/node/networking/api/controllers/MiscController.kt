package me.victoralan.software.node.networking.api.controllers

import me.victoralan.software.node.NodeManager
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/misc", produces = ["application/json"])
class MiscController {
    @GetMapping("")
    @ResponseBody
    fun default(): ResponseEntity<String> {
        return ResponseEntity("hello", HttpStatus.ACCEPTED)
    }
    @GetMapping("/checkBalance/{address}")
    fun checkBalance(@PathVariable address: String): ResponseEntity<Any> {
        return try {
            ResponseEntity.ok(NodeManager.node.getBalanceOfAddress(address))
        } catch (e: Exception){
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error checking balance: ${e.message}")
        }

    }
}