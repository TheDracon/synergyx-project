package me.victoralan

import me.victoralan.crypto.Base58.decodeChecked
import me.victoralan.crypto.ecdsa.ECDSA
import me.victoralan.software.wallet.Wallet
import java.util.*


fun main(args: Array<String>) {

    val wallet = Wallet(ECDSA().generateKeyPair())
    var generatedAddress = wallet.generateAddress(231)
    // decode the address from Base58Check encoding
    // decode the address from Base58Check encoding
    val decoded = decodeChecked(generatedAddress)

    // extract the version and the public key hash

    // extract the version and the public key hash
    val version = decoded[0]+128
    val pubKeyHash = Arrays.copyOfRange(decoded, 1, decoded.size)
    println(version)
    println(pubKeyHash.decodeToString())
//    val blockChain = BlockChain(difficulty = 2, blockSize = 100)
//    val transactions = ArrayList<Transaction>()
//    for (i in 1..100){
//        transactions.add(Transaction(User("User$i"), User("User$i"), i.toFloat()))
//    }
//
//    transactions.forEach({ x -> blockChain.addTransaction(x)})
//
//    blockChain.minePendingTransactions(User("The User"))
//    println(blockChain.toString())
}
