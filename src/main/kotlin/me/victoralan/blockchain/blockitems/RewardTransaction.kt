package me.victoralan.blockchain.blockitems

import me.victoralan.Hash
import me.victoralan.software.wallet.Address
import java.io.Serializable

class RewardTransaction(val receiver: Address, var amount: Float) : Serializable{
    var time: Long = 0
    lateinit var hash: Hash

    init {
        hash = calculateHash()
    }
    fun calculateHash(): Hash {
        return Hash("$time")
    }

    override fun toString(): String {
        return hash.toString()
    }
}