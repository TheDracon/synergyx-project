package me.victoralan.blockchain.blockitems

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import me.victoralan.Hash
import me.victoralan.crypto.Base58
import me.victoralan.crypto.SHA3
import me.victoralan.crypto.ecdsa.ECDSA
import me.victoralan.entities.User
import java.io.Serializable
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature
import java.security.spec.X509EncodedKeySpec

class Transaction(val receiver: User, val sender: User, val amount: Float) : BlockItem(System.nanoTime()),
    Serializable {
    var signature: ByteArray? = null
    init {
        hash = calculateHash()
    }
    override fun calculateHash(): Hash {
        return Hash(SHA3.hashString("$receiver $sender $amount $time"))
    }
    fun sign(privateKey: PrivateKey){
        hash = Hash(ECDSA().signMessage(hash.value, privateKey))
    }


    override fun toString(): String {
        return hash.toString()
    }
}
class TransactionDeserializer : JsonDeserializer<Transaction>() {
    override fun deserialize(jsonParser: JsonParser, deserializationContext: DeserializationContext): Transaction {
        val node: JsonNode = jsonParser.codec.readTree(jsonParser)
        val receiverNode: JsonNode = node.get("receiver")
        val receiver: User = ObjectMapper().readValue(receiverNode.traverse(), object : TypeReference<User>() {})
        val senderNode: JsonNode = node.get("receiver")
        val sender: User = ObjectMapper().readValue(receiverNode.traverse(), object : TypeReference<User>() {})
        val amount = node.get("amount").asDouble().toFloat()
        val signatureNode: JsonNode = node.get("signature")

        return Transaction(receiver, sender, amount)
    }
}