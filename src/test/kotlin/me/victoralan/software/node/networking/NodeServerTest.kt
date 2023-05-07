package me.victoralan.software.node.networking

import me.victoralan.blockchain.Block
import me.victoralan.blockchain.transactions.DebugBlockItem
import me.victoralan.blockchain.transactions.Transaction
import me.victoralan.blockchain.transactions.BlockItem
import me.victoralan.software.node.Node
import me.victoralan.software.wallet.Wallet
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.net.InetSocketAddress
import kotlin.test.assertEquals

class NodeServerTest{

    @Test
    fun test1(){
        val node = Node("thedracon", InetSocketAddress("localhost", 19101))
        node.neighbours.add(InetSocketAddress("127.0.0.1", 19102))
        node.start()

        val wallet = Wallet(password = "password1", _nodeAddress = InetSocketAddress("localhost", 19101))


        val wallet2 = Wallet(password = "password2", _nodeAddress = InetSocketAddress("localhost", 19101))


        val wallet3 = Wallet(password = "password3", _nodeAddress = InetSocketAddress("localhost", 19101))


        wallet.createAddress(0)
        wallet2.createAddress(1)
        wallet3.createAddress(2)
        val blockItems = ArrayList<BlockItem>()
        blockItems.add(DebugBlockItem(wallet.addresses[0], 100f))
        blockItems.add(DebugBlockItem(wallet2.addresses[0], 100f))
        blockItems.add(DebugBlockItem(wallet3.addresses[0], 100f))

        val block = Block(blockItems, System.nanoTime(), 1)
        node.onNewBlock(block)

        var transaction = Transaction(wallet.addresses[0], wallet2.addresses[0].address, 10f, System.nanoTime())
        transaction = wallet.sign(transaction)

        var transaction2 = Transaction(wallet2.addresses[0], wallet3.addresses[0].address, 5f, System.nanoTime())
        transaction2 = wallet2.sign(transaction2)

        var transaction3 = Transaction(wallet3.addresses[0], wallet.addresses[0].address, 1f, System.nanoTime())
        transaction3 = wallet3.sign(transaction3)

        var transaction4 = Transaction(wallet3.addresses[0], wallet2.addresses[0].address, 1f, System.nanoTime())
        transaction4 = wallet3.sign(transaction4)

        node.mineAvailable(node.miningWallet.addresses[0])

        node.onNewBlockItem(transaction)
        node.onNewBlockItem(transaction2)
        node.onNewBlockItem(transaction3)

        node.onNewBlockItem(transaction4)
        Thread.sleep(1000)
        val node2 = Node("thedracon2", InetSocketAddress("localhost", 19102))
        node2.neighbours.add(InetSocketAddress("localhost", 19101))
        node2.syncNodes()
        assertEquals(node.blockChain.toString(), node2.blockChain.toString(), "Test 1")

        node2.start()


        var transaction5 = Transaction(wallet.addresses[0], wallet2.addresses[0].address, 25f, System.nanoTime())
        transaction5 = wallet.sign(transaction5)

        var transaction6 = Transaction(wallet.addresses[0], wallet2.addresses[0].address, 15f, System.nanoTime())
        transaction6 = wallet.sign(transaction6)


        node.onNewBlockItem(transaction5)
        node.onNewBlockItem(transaction6)

        assertEquals(node.blockChain.toString(), node2.blockChain.toString(), "Test 2")
        assertTrue(node.blockChain.chain.any { it.blockItems.contains(transaction3)}, "Test 3")
    }
}