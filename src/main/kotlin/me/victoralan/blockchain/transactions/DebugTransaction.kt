package me.victoralan.blockchain.transactions

import me.victoralan.Hash
import me.victoralan.crypto.SHA3
import me.victoralan.crypto.encoder.Base58
import me.victoralan.software.wallet.Address

class DebugTransaction(val recipientAddress: Address,
                       val amount: Float,
                       override var time: Long = System.nanoTime()) : Transaction {
    override lateinit var hash: Hash
    init {
        hash = calculateHash()
    }
    override fun calculateHash(): Hash {
        return Hash(SHA3.hashString("$recipientAddress $amount $time"))
    }

    override fun toString(): String {
        return "DebugTransaction(recipientAddress=$recipientAddress, amount=$amount, time=$time, hash=$hash)"
    }
}