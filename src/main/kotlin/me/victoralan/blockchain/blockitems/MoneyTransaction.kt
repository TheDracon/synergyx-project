package me.victoralan.blockchain.blockitems

import me.victoralan.Hash
import me.victoralan.crypto.SHA3
import me.victoralan.crypto.ecdsa.ECDSA
import me.victoralan.entities.User
import java.security.PrivateKey

class MoneyTransaction(val receiver: User,
                       val sender: User,
                       val amount: Float,
                       override var time: Long = System.nanoTime()) : Transaction {
    var signature: ByteArray? = null

    override lateinit var hash: Hash
    init {
        hash = calculateHash()
    }
    override fun calculateHash(): Hash {
        return Hash(SHA3.hashString("$receiver $sender $amount $time"))
    }
    fun sign(privateKey: PrivateKey){
        signature = ECDSA().signMessage(hash.value, privateKey)
    }

    override fun toString(): String {
        return hash.toString()
    }
}
