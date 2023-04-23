package me.victoralan.blockchain.blockitems

import me.victoralan.Hash
import me.victoralan.crypto.SHA3
import me.victoralan.crypto.ecdsa.ECDSA
import me.victoralan.software.wallet.Address
import java.security.PrivateKey
import java.security.PublicKey

class MoneyTransaction(val senderAddress: Address?,
                       val recipientAddress: String,
                       val balance: Float,
                       override var time: Long = System.nanoTime()) : Transaction {
    var signature: ByteArray? = null

    override lateinit var hash: Hash
    init {
        hash = calculateHash()
    }
    override fun calculateHash(): Hash {
        return Hash(SHA3.hashString("$recipientAddress $senderAddress $balance $time"))
    }
    fun sign(privateKey: PrivateKey){
        this.signature = ECDSA().signMessage(hash.value, privateKey)
    }
    fun verify(publicKey: PublicKey): Boolean {
        if (balance <= 0f) return false

        if (ECDSA().verifySignature(hash.value, signature!!, publicKey)){

            return true
        }
        return false
    }

    override fun toString(): String {
        return hash.toString()
    }

}
