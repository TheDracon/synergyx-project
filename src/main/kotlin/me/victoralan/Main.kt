package me.victoralan

import me.victoralan.blockchain.blockitems.MoneyTransaction
import me.victoralan.crypto.encoder.Base58
import me.victoralan.software.node.Node
import me.victoralan.software.wallet.Wallet


fun main() {

    val node = Node("thedracon")
    val wallet = Wallet(password = "password1")
    val wallet2 = Wallet(password = "password2")
    val wallet3 = Wallet(password = "password3")
    wallet.createAddress(0)
    wallet2.createAddress(1)
    wallet3.createAddress(2)
    // WALLLET 1 SENDS WALLET 2 10 synergyx
    var transaction = MoneyTransaction(wallet.addresses[0], wallet2.addresses[0].address, 10f)
    println(transaction.senderAddress?.publicKey?.let { Base58.encode(it.encoded) })
    transaction = wallet.sign(transaction)

    // WALLLET 2 SENDS WALLET 3 5 synergyx
    var transaction2 = MoneyTransaction(wallet2.addresses[0], wallet3.addresses[0].address, 5f)
    transaction2 = wallet2.sign(transaction2)
    // WALLLET 3 SENDS WALLET 1 1 synergyx
    var transaction3 = MoneyTransaction(wallet3.addresses[0], wallet.addresses[0].address, 1f)
    transaction3 = wallet3.sign(transaction3)
    // WALLLET 3 SENDS WALLET 2 1 synergyx
    var transaction4 = MoneyTransaction(wallet3.addresses[0], wallet2.addresses[0].address, 1f)
    transaction4 = wallet3.sign(transaction4)

    node.startMineLoop()
    node.onNewTransaction(transaction)
    node.onNewTransaction(transaction2)
    Thread.sleep(10000)
    node.onNewTransaction(transaction3)
    node.onNewTransaction(transaction4)

}