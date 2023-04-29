package me.victoralan.software.node.networking

import me.victoralan.Hash
import me.victoralan.blockchain.Block
import me.victoralan.blockchain.transactions.Transaction
import me.victoralan.crypto.encoder.ObjectEncoder
import me.victoralan.software.node.Node
import me.victoralan.utils.NodeRequests
import me.victoralan.utils.WalletRequests
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket


class NodeServer(inetAddress: InetAddress, private val port: Int, private val node: Node) {

    private val serverSocket = ServerSocket(port, 0, inetAddress)


    fun startServer(){
        Thread{
            println("Socket server started on port $port")

            // Wait for client connections
            while (true) {
                val clientSocket = serverSocket.accept()
                println("Client connected: ${clientSocket.inetAddress.hostAddress}")
                handle(clientSocket)
            }
        }.start()

    }


    private fun handle(clientSocket: Socket){
        // Handle client connection in a new thread
        Thread {
            // Get input and output streams from the client socket
            val inputStream = DataInputStream(clientSocket.getInputStream())

            // check if client is a node or a wallet

            val isWallet = inputStream.readBoolean()
            if (isWallet){
                handleWallet(clientSocket)
            } else{
                handleNode(clientSocket)
            }

        }.start()
    }

    /**
     * @param address The address of the node to get the blockChain from
     * @return A pair containing the length and the blockchain itself
     */
    fun getBlockChainOf(address: InetSocketAddress, minimumLength: Int = 0): Pair<Int, ByteArray>?{
        val clientSocket = Socket()
        clientSocket.connect(address)
        val inputStream = DataInputStream(clientSocket.getInputStream())
        val outputStream = DataOutputStream(clientSocket.getOutputStream())

        outputStream.writeBoolean(false)
        outputStream.writeInt(NodeRequests.GET_BLOCKCHAIN.value)
        if (inputStream.readInt() != 0) return null
        val length = inputStream.readInt()
        var blockChain: ByteArray? = null
        if (length > minimumLength){
            // If true then you can send the full chain
            outputStream.writeBoolean(true)
            blockChain = inputStream.readAllBytes()
        } else{
            outputStream.writeBoolean(false)
        }
        return if (blockChain == null) null
        else Pair(length, blockChain)

    }


    /**
     * Broadcasts to all nodes the transactions
     * @param nodesAddresses A list of all the nodes you want to broadcast the transaction to
     * @param transaction The transaction you want to broadcast
     * @return A validityList where each integer is the validity that each node has returned
     */
    fun broadcastNewTransaction(nodesAddresses: ArrayList<InetSocketAddress>, transaction: Transaction): ArrayList<Int>{
        val validityList: ArrayList<Int> = ArrayList()
        for (nodeAddress in nodesAddresses){
            val clientSocket = Socket()
            clientSocket.connect(nodeAddress)
            val inputStream = DataInputStream(clientSocket.getInputStream())
            val outputStream = DataOutputStream(clientSocket.getOutputStream())
            val encoder = ObjectEncoder()
            outputStream.writeInt(NodeRequests.NEW_TRANSACTION.value)
            if (inputStream.readInt() == 0){
                outputStream.write(encoder.encode(transaction))
                validityList.add(inputStream.readInt())
            } else{
                throw RuntimeException("Han error with the connection has occurred")
            }
        }
        return validityList
    }
    /**
     * Broadcasts to all nodes the block
     * @param nodesAddresses A list of all the nodes you want to broadcast the transaction to
     * @param block The block you want to broadcast
     * @return A list of booleans where each one is whether the node accepted the block or not
     */
    fun broadcastNewBlock(nodesAddresses: ArrayList<InetSocketAddress>, block: Block): ArrayList<Boolean>{
        val acceptedList: ArrayList<Boolean> = ArrayList()
        for (nodeAddress in nodesAddresses){
            val clientSocket = Socket()
            clientSocket.connect(nodeAddress)
            val inputStream = DataInputStream(clientSocket.getInputStream())
            val outputStream = DataOutputStream(clientSocket.getOutputStream())
            val encoder = ObjectEncoder()
            outputStream.writeInt(NodeRequests.NEW_BLOCK.value)
            if (inputStream.readInt() == 0){
                outputStream.write(encoder.encode(block))
                acceptedList.add(inputStream.readBoolean())
            } else{
                throw RuntimeException("Han error with the connection has occurred")
            }
        }
        return acceptedList
    }

    private fun handleWallet(walletSocket: Socket){
        val inputStream = DataInputStream(walletSocket.getInputStream())
        val outputStream = DataOutputStream(walletSocket.getOutputStream())

        when(inputStream.readInt()){
            WalletRequests.NEW_TRANSACTION.value -> handleNewTransaction(walletSocket)
            WalletRequests.CHECK_IF_TRANSACTION_VALIDATED.value -> handleCheckIfTransactionValidated(walletSocket)
            WalletRequests.CHECK_BALANCE.value -> handleCheckBalance(walletSocket)
            // Invalid request if not one of above
            else -> outputStream.writeInt(-1)
        }
    }
    private fun handleCheckIfTransactionValidated(walletSocket: Socket){
        val inputStream = DataInputStream(walletSocket.getInputStream())
        val outputStream = DataOutputStream(walletSocket.getOutputStream())
        outputStream.writeInt(0)

        val hashOfTransaction =  Hash(inputStream.readAllBytes())
        val transaction = node.searchTransactionByHash(hashOfTransaction)
        outputStream.writeBoolean(transaction != null)
    }

    private fun handleCheckBalance(walletSocket: Socket){
        val inputStream = DataInputStream(walletSocket.getInputStream())
        val outputStream = DataOutputStream(walletSocket.getOutputStream())
        outputStream.writeInt(0)
        val address = inputStream.readUTF()
        outputStream.writeFloat(node.getBalanceOfAddress(address))
    }
    private fun handleNode(nodeSocket: Socket){
        val inputStream = DataInputStream(nodeSocket.getInputStream())
        val outputStream = DataOutputStream(nodeSocket.getOutputStream())

        when (inputStream.readInt()){
            NodeRequests.NEW_TRANSACTION.value -> handleNewTransaction(nodeSocket)
            NodeRequests.NEW_BLOCK.value -> handleNewBlock(nodeSocket)
            NodeRequests.GET_BLOCKCHAIN.value -> handleGetBlockChain(nodeSocket)
            //TODO(USE THE RESOLVE ISSUE)
            else -> outputStream.writeInt(-1)
        }
    }

    private fun handleNewTransaction(clientSocket: Socket){
        val inputStream = DataInputStream(clientSocket.getInputStream())
        val outputStream = DataOutputStream(clientSocket.getOutputStream())
        outputStream.writeInt(0)

        val transactionData: ByteArray = inputStream.readAllBytes()
        val decoder = ObjectEncoder()
        val transaction = decoder.decode<Transaction>(transactionData)
        val transactionValidity = node.onNewTransaction(transaction)

        outputStream.writeInt(transactionValidity)
    }
    private fun handleNewBlock(nodeSocket: Socket){
        val inputStream = DataInputStream(nodeSocket.getInputStream())
        val outputStream = DataOutputStream(nodeSocket.getOutputStream())
        outputStream.writeInt(0)

        val blockData: ByteArray = inputStream.readAllBytes()
        val decoder = ObjectEncoder()
        val block = decoder.decode<Block>(blockData)
        val isBlockValid = node.onNewBlock(block)
        outputStream.writeBoolean(isBlockValid)
    }

    private fun handleGetBlockChain(nodeSocket: Socket){
        val inputStream = DataInputStream(nodeSocket.getInputStream())
        val outputStream = DataOutputStream(nodeSocket.getOutputStream())
        outputStream.writeInt(0)

        outputStream.writeInt(node.blockChain.chain.size)
        val encoder = ObjectEncoder()
        val sendBlockChain = inputStream.readBoolean()
        if (sendBlockChain){
            outputStream.write(encoder.encode(node.blockChain))
        }
    }
}

