package me.victoralan

import me.victoralan.crypto.SHA3

class Block(val transactions: ArrayList<Transaction>, val time: Long, val index: Long) {
    var hash: Hash
    var previousBlockHash: Hash = Hash("none")
    var nonce: Long = 0
    init {
        hash = calculateHash()
    }
    fun mine(difficulty: Int, maxTime: Long = -1): Boolean{
        while (!Hash.getBinaryString(hash.value).startsWith("0".repeat(difficulty))){
            nonce++
            hash = calculateHash()
            if (nonce == maxTime){
                println("Max time exceeded at $maxTime")
                return false
            }
        }
        println("Block mined, nonce to solve PoW: $nonce")

        return true
    }
    fun calculateHash(): Hash {
        var transactionsString = ""
        for (transaction in transactions){
            transactionsString += transaction
        }
        return Hash(SHA3.hashString("$transactionsString $previousBlockHash $time $index $nonce"))
    }
}