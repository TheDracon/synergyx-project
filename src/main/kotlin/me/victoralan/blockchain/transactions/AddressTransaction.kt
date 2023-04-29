package me.victoralan.blockchain.transactions

import me.victoralan.Hash
import me.victoralan.crypto.SHA3
import me.victoralan.crypto.encoder.Base58
import me.victoralan.software.wallet.Address
import java.security.PublicKey

class AddressTransaction(
    val publicKey: PublicKey,
    var address: Address,
    override var time: Long = System.nanoTime()) : Transaction {
    constructor(address: Address) : this(address.publicKey, address)

    override lateinit var hash: Hash
    init {
        hash = calculateHash()
    }
    override fun calculateHash(): Hash {
        return Hash(SHA3.hashString("$publicKey $address $time"))
    }

    override fun toString(): String {
        return "AddressTransaction(publicKey=${Base58.encode(publicKey.encoded)}, address=$address, time=$time)"
    }
}