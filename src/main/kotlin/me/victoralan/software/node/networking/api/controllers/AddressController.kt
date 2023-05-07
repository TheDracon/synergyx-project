package me.victoralan.software.node.networking.api.controllers

import me.victoralan.blockchain.transactions.AddressBlockItem
import me.victoralan.software.node.NodeManager
import me.victoralan.software.wallet.Address
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/address", produces = ["application/json"])
class AddressController {
    @PostMapping("/new")
    fun newAddress(@RequestBody addressBlockItem: Address) : ResponseEntity<Any> {
        return try {
            val validity = NodeManager.node.onNewBlockItem(AddressBlockItem(addressBlockItem))
            if (validity == 0){
                ResponseEntity(0, HttpStatus.ACCEPTED)
            } else{
                ResponseEntity(HttpStatus.BAD_REQUEST)
            }
        } catch (exception: Exception){
            ResponseEntity(exception, HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }
}