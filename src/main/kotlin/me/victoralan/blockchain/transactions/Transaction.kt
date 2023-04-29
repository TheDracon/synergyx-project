package me.victoralan.blockchain.transactions

import me.victoralan.blockchain.Hash
import java.io.Serializable

interface Transaction : Serializable{
    var time: Long
    var hash: Hash
    fun calculateHash(): Hash

    override fun toString(): String

}
