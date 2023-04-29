package me.victoralan

import me.victoralan.blockchain.Block
import me.victoralan.blockchain.transactions.DebugTransaction
import me.victoralan.blockchain.transactions.MoneyTransaction
import me.victoralan.blockchain.transactions.Transaction
import me.victoralan.crypto.ecdsa.ECDSA
import me.victoralan.software.node.Node
import me.victoralan.software.wallet.Wallet


fun main() {
    val node = Node("thedracon")
    val wallet = Wallet(ECDSA().generateKeyPair(), "password1")
    val wallet2 = Wallet(ECDSA().generateKeyPair(), "password2")
    val wallet3 = Wallet(ECDSA().generateKeyPair(), "password3")
    wallet.createAddress(0)
    wallet2.createAddress(1)
    wallet3.createAddress(2)
    val transactions = ArrayList<Transaction>()
    transactions.add(DebugTransaction(wallet.addresses[0], 100f))
    transactions.add(DebugTransaction(wallet2.addresses[0], 100f))
    transactions.add(DebugTransaction(wallet3.addresses[0], 100f))

    val block = Block(transactions, System.nanoTime(), 0)

    node.onNewBlock(block)

    var transaction = MoneyTransaction(wallet.addresses[0], wallet2.addresses[0].address, 10f, System.nanoTime())
    transaction = wallet.sign(transaction)

    var transaction2 = MoneyTransaction(wallet2.addresses[0], wallet3.addresses[0].address, 5f, System.nanoTime())
    transaction2 = wallet2.sign(transaction2)

    var transaction3 = MoneyTransaction(wallet3.addresses[0], wallet.addresses[0].address, 1f, System.nanoTime())
    transaction3 = wallet3.sign(transaction3)

    var transaction4 = MoneyTransaction(wallet3.addresses[0], wallet2.addresses[0].address, 1f, System.nanoTime())
    transaction4 = wallet3.sign(transaction4)

    var transaction5 = MoneyTransaction(wallet.addresses[0], wallet2.addresses[0].address, 25f, System.nanoTime())
    transaction5 = wallet.sign(transaction5)

    var transaction6 = MoneyTransaction(wallet.addresses[0], wallet2.addresses[0].address, 15f, System.nanoTime())
    transaction6 = wallet.sign(transaction6)

    var transaction7 = MoneyTransaction(wallet3.addresses[0], wallet.addresses[0].address, -1000f, System.nanoTime())
    transaction7 = wallet3.sign(transaction7)


    node.mineAvailable(node.miningWallet.addresses[0])
    node.onNewTransaction(transaction)
    node.onNewTransaction(transaction2)
    node.onNewTransaction(transaction3)
    node.onNewTransaction(transaction4)
    node.onNewTransaction(transaction5)
    node.onNewTransaction(transaction6)
    node.onNewTransaction(transaction7)
    Thread.sleep(5000)
    println("FINAL RESULTS OF TEST: ")
    println("WALLET 1 BALANCE: ${node.getBalanceOfAddress(wallet.addresses.first().address, true)} SRX")
    println("WALLET 2 BALANCE: ${node.getBalanceOfAddress(wallet2.addresses.first().address, true)} SRX")
    println("WALLET 3 BALANCE: ${node.getBalanceOfAddress(wallet3.addresses.first().address, true)} SRX")

}
