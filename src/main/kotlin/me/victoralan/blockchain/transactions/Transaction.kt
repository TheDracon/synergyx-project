package me.victoralan.blockchain.transactions

import me.victoralan.blockchain.Hash
import me.victoralan.crypto.SHA3
import me.victoralan.crypto.ecdsa.ECDSA
import me.victoralan.crypto.encoder.Base58
import me.victoralan.software.wallet.Address
import java.security.PrivateKey
import java.security.PublicKey

class Transaction(val senderAddress: Address?,
                  val recipientAddress: String,
                  val amount: Float,
                  override var time: Long = System.nanoTime()) : BlockItem {
    var signature: ByteArray? = null

    override lateinit var hash: Hash
    init {
        hash = calculateHash()
    }
    override fun calculateHash(): Hash {
        return Hash(SHA3.hashString("$recipientAddress $senderAddress $amount $time"))
    }



    fun sign(privateKey: PrivateKey){
        this.signature = ECDSA().signMessage(hash.value, privateKey)
    }
    fun verify(publicKey: PublicKey): Boolean {
        if (amount <= 0f) return false

        if (ECDSA().verifySignature(hash.value, signature!!, publicKey)){

            return true
        }
        return false
    }

    override fun toString(): String {
        return "Transaction(senderAddress=$senderAddress, recipientAddress=$recipientAddress, amount=$amount, time=$time, signature=${Base58.encode(signature!!)}, hash=$hash)"
    }

}
