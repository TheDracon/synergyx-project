package me.victoralan.blockchain.transactions

import me.victoralan.Hash
import me.victoralan.software.wallet.Address
import java.io.Serializable

class CoinBaseTransaction(val recipientAddress: Address, var amount: Float) : Serializable{
    var time: Long = 0
    var hash: Hash

    init {
        hash = calculateHash()
    }
    fun calculateHash(): Hash {
        return Hash("$time")
    }

    override fun toString(): String {
        return "CoinBaseTransaction(recipientAddress=$recipientAddress, amount=$amount, time=$time, hash=$hash)"
    }
}