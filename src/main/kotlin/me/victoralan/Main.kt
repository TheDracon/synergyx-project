package me.victoralan

import me.victoralan.entities.Miner
import me.victoralan.entities.Receiver
import me.victoralan.entities.Sender

fun main(args: Array<String>) {
    val blockChain = BlockChain(difficulty = 2, blockSize = 100)
    val transactions = ArrayList<Transaction>()
    for (i in 1..100){
        transactions.add(Transaction(Sender("Sender$i"), Receiver("Receiver$i"), i.toFloat()))
    }

    transactions.forEach({ x -> blockChain.addTransaction(x)})

    blockChain.minePendingTransactions(Miner("The Miner"))
    println(blockChain.toString())
}
