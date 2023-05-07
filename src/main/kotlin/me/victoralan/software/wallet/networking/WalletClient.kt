package me.victoralan.software.wallet.networking

import me.victoralan.blockchain.Hash
import me.victoralan.blockchain.transactions.BlockItem
import me.victoralan.crypto.encoder.ObjectEncoder
import me.victoralan.utils.WalletRequests
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.InetSocketAddress
import java.net.Socket

class WalletClient(var nodeAddress: InetSocketAddress?) {
    var inputStream: DataInputStream? = null
    var outputStream: DataOutputStream? = null
    private fun connectToNode(){
        if (nodeAddress == null){
            throw java.lang.RuntimeException("Node address never set")
        }
        val socket = Socket()

        socket.connect(nodeAddress!!)
        inputStream = DataInputStream(socket.getInputStream())
        outputStream = DataOutputStream(socket.getOutputStream())
        outputStream!!.writeBoolean(true)
    }
    fun isBlockItemValid(hash: Hash): Boolean{
        connectToNode()

        if (inputStream != null && outputStream != null){

            outputStream!!.writeInt(WalletRequests.CHECK_IF_BLOCKITEM_VALIDATED.value)
            if (inputStream!!.readInt() == 0){
                outputStream!!.writeBytes(hash.value)
                return inputStream!!.readBoolean()
            } else throw RuntimeException("Han error with the connection has occurred")
        } else throw RuntimeException("Have to connected to node before sending any request")
    }
    /**
     * To send a blockItem to node
     * @param blockItem The blockItem to send
     * @return Whether the blockItem is valid or not
     */
    fun sendBlockItem(blockItem: BlockItem): Boolean{
        connectToNode()
        if (inputStream != null && outputStream != null){

            outputStream!!.writeInt(WalletRequests.NEW_BLOCKITEM.value)
            if (inputStream!!.readInt() == 0){
                val encoder = ObjectEncoder()
                val encoded: ByteArray = encoder.encode(blockItem)
                outputStream!!.writeBytes(encoded)
                return inputStream!!.readInt() == 0
            } else throw RuntimeException("Han error with the connection has occurred")
        } else throw RuntimeException("Have to connected to node before sending any request")
    }

    fun checkBalance(address: String): Float {
        connectToNode()

        if (inputStream != null && outputStream != null){
            outputStream!!.writeInt(WalletRequests.CHECK_BALANCE.value)
            if (inputStream!!.readInt() == 0){
                outputStream!!.writeUTF(address)
                return inputStream!!.readFloat()
            } else throw RuntimeException("Han error with the connection has occurred")
        } else throw RuntimeException("Have to connected to node before sending any request")
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