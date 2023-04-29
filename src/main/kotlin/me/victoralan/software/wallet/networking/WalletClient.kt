package me.victoralan.software.wallet.networking

import me.victoralan.Hash
import me.victoralan.blockchain.transactions.Transaction
import me.victoralan.crypto.encoder.ObjectEncoder
import me.victoralan.utils.WalletRequests
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.InetAddress
import java.net.Socket

class WalletClient {
    var inputStream: DataInputStream? = null
    var outputStream: DataOutputStream? = null
    fun connectToNode(nodeAddress: InetAddress, port: Int){
        println("connecting...")
        val socket = Socket(nodeAddress, port)
        println("Connected to server")
        inputStream = DataInputStream(socket.getInputStream())
        outputStream = DataOutputStream(socket.getOutputStream())
    }

    fun isTransactionValid(hash: Hash): Boolean{
        if (inputStream != null && outputStream != null){

            outputStream!!.writeInt(WalletRequests.CHECK_IF_TRANSACTION_VALIDATED.value)
            if (inputStream!!.readInt() == 0){
                outputStream!!.write(hash.value)
                return inputStream!!.readBoolean()
            } else throw RuntimeException("Han error with the connection has occurred")
        } else throw RuntimeException("Have to connected to node before sending any request")
    }
    /**
     * To send a transaction to node
     * @param transaction The transaction to send
     * @return Whether the transaction is valid or not
     */
    fun sendTransaction(transaction: Transaction): Boolean{
        if (inputStream != null && outputStream != null){

            outputStream!!.writeInt(WalletRequests.NEW_TRANSACTION.value)
            if (inputStream!!.readInt() == 0){
                val encoder = ObjectEncoder()
                outputStream!!.write(encoder.encode(transaction))
                return inputStream!!.readInt() == 0
            } else throw RuntimeException("Han error with the connection has occurred")
        } else throw RuntimeException("Have to connected to node before sending any request")
    }

    fun checkBalance(address: String): Float {
        if (inputStream != null && outputStream != null){
            outputStream!!.writeInt(WalletRequests.CHECK_BALANCE.value)
            if (inputStream!!.readInt() == 0){
                outputStream!!.writeUTF(address)
                return inputStream!!.readFloat()
            } else throw RuntimeException("Han error with the connection has occurred")
        } else throw RuntimeException("Have to connected to node before sending any request")
    }
}