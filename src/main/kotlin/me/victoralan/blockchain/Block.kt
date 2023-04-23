package me.victoralan.blockchain

import me.victoralan.Hash
import me.victoralan.blockchain.blockitems.RewardTransaction
import me.victoralan.blockchain.blockitems.Transaction
import me.victoralan.crypto.SHA3
import java.io.Serializable

class Block(val transactions: ArrayList<Transaction>, val time: Long, val index: Long) : Serializable {
    var hash: Hash
    var previousBlockHash: Hash = Hash("none")
    var coinBaseTransaction: RewardTransaction? = null
    var nonce: Long = 0
    init {
        hash = calculateHash()
    }

    fun mine(difficulty: Int, maxTime: Long = -1): Boolean {
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
            transactionsString += transaction.hash
        }
        return Hash(SHA3.hashString("$transactionsString ${coinBaseTransaction?.hash} $previousBlockHash $time $index $nonce"))
    }

    override fun toString(): String {
        return hash.toString()
    }
}
