package me.victoralan

import me.victoralan.crypto.SHA3
import me.victoralan.entities.Receiver
import me.victoralan.entities.Sender

class Transaction(val sender: Sender, val receiver: Receiver, val amount: Float) {
    val time: Long = System.nanoTime()
    val hash: Hash
    init {
        hash = calculateHash()
    }
    fun calculateHash(): Hash {
        return Hash(SHA3.hashString("$sender $receiver $amount $time"))
    }

    override fun toString(): String {
        return hash.toString()
    }
}