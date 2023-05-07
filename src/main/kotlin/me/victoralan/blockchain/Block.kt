package me.victoralan.blockchain

import com.fasterxml.jackson.annotation.JsonIgnore
import me.victoralan.blockchain.transactions.CoinBaseTransaction
import me.victoralan.blockchain.transactions.BlockItem
import me.victoralan.crypto.SHA3
import java.io.Serializable
import java.security.SecureRandom

class Block(val blockItems: ArrayList<BlockItem>, val time: Long, val index: Int) : Serializable {
    var hash: Hash
    var previousBlockHash: Hash = Hash("none")
    var coinBaseTransaction: CoinBaseTransaction? = null
    var nonce: Long = SecureRandom().nextLong()

    init {
        hash = calculateHash()
    }

    fun mine(difficulty: Int): Boolean{
        while (!hash.binaryString().startsWith("0".repeat(difficulty))) {
            nonce++
            this.hash = calculateHash()
        }
        return true
    }
    @JsonIgnore
    fun isGenesis(): Boolean{
        return (blockItems.isEmpty() && coinBaseTransaction == null && index == 0)
    }
    fun calculateHash(): Hash {
        var blockItemsString = ""
        for (blockItem in blockItems){
            blockItemsString += blockItem.hash
        }
        return Hash(SHA3.hashString("$blockItemsString ${coinBaseTransaction?.hash} $previousBlockHash $time $index $nonce"))
    }

    override fun toString(): String {
        return hash.toString()
    }
}
