package me.victoralan.software.node.networking

import me.victoralan.blockchain.Hash
import me.victoralan.blockchain.Block
import me.victoralan.blockchain.transactions.BlockItem
import me.victoralan.crypto.encoder.ObjectEncoder
import me.victoralan.software.node.Node
import me.victoralan.utils.NodeRequests
import me.victoralan.utils.WalletRequests
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.ConnectException
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException


class NodeServer(var host: InetSocketAddress, private val node: Node) {

    private val serverSocket = ServerSocket(host.port, 0, host.address)


    fun startServer(){
        Thread{
            println("Socket server started on port ${host.port}")

            // Wait for client connections
            while (true) {
                val clientSocket = serverSocket.accept()
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
     * Broadcasts to all nodes the blockItem
     * @param nodesAddresses A list of all the nodes you want to broadcast the blockItem to
     * @param blockItem The blockItem you want to broadcast
     * @return A validityList where each integer is the validity that each node has returned
     */
    fun broadcastNewBlockItem(nodesAddresses: ArrayList<InetSocketAddress>, blockItem: BlockItem): ArrayList<Int>{
        val validityList: ArrayList<Int> = ArrayList()
        for (nodeAddress in nodesAddresses){

            val clientSocket = Socket()
            try{
                clientSocket.connect(nodeAddress)
            } catch (e: ConnectException){
                continue
            }
            val inputStream = DataInputStream(clientSocket.getInputStream())
            val outputStream = DataOutputStream(clientSocket.getOutputStream())
            outputStream.writeBoolean(false)
            val encoder = ObjectEncoder()
            outputStream.writeInt(NodeRequests.NEW_BLOCKITEM.value)
            if (inputStream.readInt() == 0){
                outputStream.writeBytes(encoder.encode(blockItem))
                validityList.add(inputStream.readInt())
            } else{
                throw RuntimeException("Han error with the connection has occurred")
            }
        }
        return validityList
    }
    /**
     * Broadcasts to all nodes the block
     * @param nodesAddresses A list of all the nodes you want to broadcast the block to
     * @param block The block you want to broadcast
     * @return A list of booleans where each one is whether the node accepted the block or not
     */
    fun broadcastNewBlock(nodesAddresses: ArrayList<InetSocketAddress>, block: Block): ArrayList<Boolean>{
        val acceptedList: ArrayList<Boolean> = ArrayList()
        for (nodeAddress in nodesAddresses){
            val clientSocket = Socket()
            try{
                clientSocket.connect(nodeAddress)
            } catch (e: ConnectException){
                continue
            }
            val inputStream = DataInputStream(clientSocket.getInputStream())
            val outputStream = DataOutputStream(clientSocket.getOutputStream())
            outputStream.writeBoolean(false)
            val encoder = ObjectEncoder()
            outputStream.writeInt(NodeRequests.NEW_BLOCK.value)

            if (inputStream.readInt() == 0){
                outputStream.writeBytes(encoder.encode(block))
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
            WalletRequests.NEW_BLOCKITEM.value -> handleNewBlockItem(walletSocket)
            WalletRequests.CHECK_IF_BLOCKITEM_VALIDATED.value -> handleCheckIfBlockItemValidated(walletSocket)
            WalletRequests.CHECK_BALANCE.value -> handleCheckBalance(walletSocket)
            // Invalid request if not one of above
            else -> outputStream.writeInt(-1)
        }
        walletSocket.close()
        outputStream.close()
        inputStream.close()
    }

    private fun handleCheckIfBlockItemValidated(walletSocket: Socket){
        val inputStream = DataInputStream(walletSocket.getInputStream())
        val outputStream = DataOutputStream(walletSocket.getOutputStream())
        outputStream.writeInt(0)

        val hashOfBlockItem =  Hash(inputStream.readAllBytesSent())
        val blockItem = node.searchBlockItemByHash(hashOfBlockItem)
        outputStream.writeBoolean(blockItem != null)
    }

    private fun handleCheckBalance(walletSocket: Socket){
        val inputStream = DataInputStream(walletSocket.getInputStream())
        val outputStream = DataOutputStream(walletSocket.getOutputStream())
        outputStream.writeInt(0)
        val address = inputStream.readUTF()
        //TODO("NOT DEBUG MODE")
        outputStream.writeFloat(node.getBalanceOfAddress(address, true))
    }

    private fun handleNode(nodeSocket: Socket){
        val inputStream = DataInputStream(nodeSocket.getInputStream())
        val outputStream = DataOutputStream(nodeSocket.getOutputStream())

        when (inputStream.readInt()){
            NodeRequests.NEW_BLOCKITEM.value -> handleNewBlockItem(nodeSocket)
            NodeRequests.NEW_BLOCK.value -> handleNewBlock(nodeSocket)
            NodeRequests.GET_BLOCKCHAIN.value -> handleGetBlockChain(nodeSocket)
            //TODO(USE THE RESOLVE ISSUE)
            else -> outputStream.writeInt(-1)
        }
    }

    private fun handleNewBlockItem(clientSocket: Socket){
        val inputStream = DataInputStream(clientSocket.getInputStream())
        val outputStream = DataOutputStream(clientSocket.getOutputStream())
        outputStream.writeInt(0)
        val blockItemData = inputStream.readAllBytesSent()


        val decoder = ObjectEncoder()
        val blockItem = decoder.decode<BlockItem>(blockItemData)
        val blockItemValidity = node.onNewBlockItem(blockItem)

        outputStream.writeInt(blockItemValidity)
        Thread.sleep(100)
    }
    private fun handleNewBlock(nodeSocket: Socket){
        val inputStream = DataInputStream(nodeSocket.getInputStream())
        val outputStream = DataOutputStream(nodeSocket.getOutputStream())
        outputStream.writeInt(0)

        val blockData: ByteArray = inputStream.readAllBytesSent()
        val decoder = ObjectEncoder()
        val block = decoder.decode<Block>(blockData)
        val isBlockValid = node.onNewBlock(block)
        outputStream.writeBoolean(isBlockValid)
    }

    private fun handleGetBlockChain(nodeSocket: Socket){

        val inputStream = DataInputStream(nodeSocket.getInputStream())
        val outputStream = DataOutputStream(nodeSocket.getOutputStream())
        Thread.sleep(10)
        outputStream.writeInt(0)

        outputStream.writeInt(node.blockChain.chain.size)
        try {
            val sendBlockChain = inputStream.readBoolean()
            if (sendBlockChain){
                val encoder = ObjectEncoder()
                outputStream.writeBytes(encoder.encode(node.blockChain))
            }
        } catch (ignore: SocketException){}
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
        val response = inputStream.readInt()
        if (response != 0) return null

        val length = inputStream.readInt()
        var blockChain: ByteArray? = null

        if (length > minimumLength){
            // If true then you can send the full chain
            outputStream.writeBoolean(true)
            blockChain = inputStream.readAllBytesSent()
        } else{
            outputStream.writeBoolean(false)
        }
        clientSocket.close()
        return if (blockChain == null) null
        else Pair(length, blockChain)

    }
    fun DataInputStream.readAllBytesSent(): ByteArray{
        val len = this.readInt()
        return this.readNBytes(len)
    }
    fun DataOutputStream.writeBytes(bytes: ByteArray){
        this.writeInt(bytes.size)
        this.write(bytes)
    }
}

