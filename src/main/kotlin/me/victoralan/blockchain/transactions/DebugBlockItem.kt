package me.victoralan.blockchain.transactions

import me.victoralan.blockchain.Hash
import me.victoralan.crypto.SHA3
import me.victoralan.software.wallet.Address

class DebugBlockItem(val recipientAddress: Address,
                     val amount: Float,
                     override var time: Long = System.nanoTime()) : BlockItem {
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