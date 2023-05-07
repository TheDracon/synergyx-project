package me.victoralan.software.node.networking.api.controllers

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/", produces = ["application/json"])
class MainController {

    @GetMapping("/")
    fun helloWord(): List<String> {
        return listOf("Hello World!")
    }

}