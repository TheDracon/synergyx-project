package me.victoralan.blockchain

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import me.victoralan.Hash
import me.victoralan.blockchain.blockitems.Address
import me.victoralan.blockchain.blockitems.BlockItem
import me.victoralan.blockchain.blockitems.Transaction
import me.victoralan.crypto.Base58
import me.victoralan.crypto.SHA3
import java.io.Serializable
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec

class Block(val transactions: ArrayList<BlockItem>, val time: Long, val index: Long) : Serializable {
    var hash: Hash
    var previousBlockHash: Hash = Hash("none")
    var nonce: Long = 0
    init {
        hash = calculateHash()
    }

    constructor(nonce: Long, hash: Hash, previousBlockHash: Hash, transactions: ArrayList<BlockItem>, time: Long, index: Long): this(transactions, time, index){
        this.nonce = nonce
        this.hash = hash
        this.previousBlockHash = previousBlockHash
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
class BlockDeserializer : JsonDeserializer<Block>(){
    override fun deserialize(jsonParser: JsonParser, p1: DeserializationContext): Block {
        val node: JsonNode = jsonParser.codec.readTree(jsonParser)
        val nonce: Long = node.get("nonce").asLong()
        val hash: Hash = Hash(node.get("hash").asText())
        val previousBlockHash: Hash = Hash(node.get("previousBlockHash").asText())
        val transactionsNode: JsonNode = node.get("transactions")
        val transactions: ArrayList<BlockItem> = ObjectMapper().readValue(transactionsNode.traverse(), object : TypeReference<ArrayList<BlockItem>>() {})

        println(transactions.get(0).hash)
        val time: Long = node.get("time").asLong()
        val index: Long = node.get("index").asLong()

        return Block(nonce, hash, previousBlockHash, transactions, time, index)
    }

}