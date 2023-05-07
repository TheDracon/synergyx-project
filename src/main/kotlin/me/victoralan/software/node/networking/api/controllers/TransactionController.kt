package me.victoralan.software.node.networking.api.controllers

import me.victoralan.blockchain.transactions.Transaction
import me.victoralan.software.node.NodeManager
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/transaction", produces = ["application/json"])
class TransactionController {

    @PostMapping("/new")
    fun newTransaction(@RequestBody transaction: Transaction) : ResponseEntity<List<Int>> {
        try {
            transaction.time = System.nanoTime()
            val validity = NodeManager.node.onNewBlockItem(transaction)
            if (validity == 0){
                return ResponseEntity(listOf(validity), HttpStatus.ACCEPTED)
            }
            return ResponseEntity(listOf(validity), HttpStatus.BAD_REQUEST)
        } catch (e: Exception){
            return ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }
    @GetMapping("/isValid")
    fun checkTransactionValidity(@RequestBody hash: ByteArray):  List<Boolean>{
        return listOf(NodeManager.node.searchBlockItemByHash(hash) != null)
    }
}