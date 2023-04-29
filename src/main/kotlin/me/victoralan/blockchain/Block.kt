package me.victoralan.blockchain

import me.victoralan.Hash
import me.victoralan.blockchain.transactions.CoinBaseTransaction
import me.victoralan.blockchain.transactions.Transaction
import me.victoralan.crypto.SHA3
import me.victoralan.software.node.Node
import java.io.Serializable
import java.security.SecureRandom

class Block(val transactions: ArrayList<Transaction>, val time: Long, val index: Long) : Serializable {
    var hash: Hash
    var previousBlockHash: Hash = Hash("none")
    var coinBaseTransaction: CoinBaseTransaction? = null
    var nonce: Long = SecureRandom().nextLong()
    init {
        hash = calculateHash()
    }

    fun mine(difficulty: Int, node: Node): Boolean{
        while (hash.binaryString().startsWith("0".repeat(difficulty)) && node.currentBlockMiningHash.value.contentEquals(this.hash.value)){
            nonce++
            hash = calculateHash()
        }
        return node.currentBlockMiningHash.value.contentEquals(this.hash.value)
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
