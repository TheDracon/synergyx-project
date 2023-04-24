package me.victoralan.software.node

import me.victoralan.blockchain.Block
import me.victoralan.blockchain.BlockChain
import me.victoralan.blockchain.transactions.*
import me.victoralan.crypto.encoder.Base58
import me.victoralan.crypto.encoder.ObjectEncoder
import me.victoralan.software.node.networking.NodeServer
import me.victoralan.software.wallet.Address
import me.victoralan.software.wallet.Wallet
import java.io.File
import java.net.InetAddress
import java.security.PublicKey

class Node(walletPassword: String) {
    var blockChain: BlockChain = BlockChain(difficulty = 30, blockSize = 2)
    var miningWallet: Wallet
    var addressCache: List<Address> = ArrayList()
    val server: NodeServer = NodeServer(InetAddress.getLocalHost(), 8081)

    init {
        miningWallet = Wallet(password = walletPassword)
    }
    fun onNewBlock(block: Block): Boolean{

        if (isBlockValid(block)){
            // ADD THE BLOCK
            blockChain.addBlock(block)

            println(block.transactions.size)
            for (transaction in block.transactions){
                for (pendingTransaction in blockChain.pendingTransactions.clone() as ArrayList<Transaction>){
                    if (transaction.hash == pendingTransaction.hash){
                        //REMOVE FROM PENDING TRANSACTIONS
                        if (blockChain.pendingTransactions.contains(pendingTransaction)){
                            blockChain.pendingTransactions.remove(pendingTransaction)
                        }

                    }
                }
            }
        }
        return true
    }
    private fun isBlockValid(block: Block): Boolean{
        //CHECK IF BLOCK IS FULL
        if (block.transactions.filterIsInstance<DebugTransaction>().isNotEmpty()){
            return true
        }
        if (block.transactions.size == blockChain.blockSize){
            //CHECK IF CALCULATION OF HASH IS THE SAME AS THE HASH
            if (block.hash.value.contentEquals(block.calculateHash().value)){

                //CHECK IF BLOCK HASH STARTS WITH difficulty AMOUNT OF 0's
                if (block.hash.binaryString().startsWith("0".repeat(blockChain.difficulty))){

                    //CHECK IF HASH OF PREVIOUS BLOCK IS VALID
                    println("PREVIOUS: ${Base58.encode(blockChain.getLastBlock().hash.value)}")
                    println("ACTUAL: ${Base58.encode(block.previousBlockHash.value)}")

                    if (block.previousBlockHash.value.contentEquals(blockChain.getLastBlock().hash.value)){
                        if (block.coinBaseTransaction == null) return false
                        if (block.coinBaseTransaction!!.amount != blockChain.reward) return false
                        //CHECK IF TRANSACTIONS OF BLOCK ARE VALID
                        for (transaction in block.transactions){
                            if (!isTransactionValid(transaction)){
                                return false
                            }
                        }

                        return true
                    }
                }
            }
        }
        return false
    }

    fun saveBlockChain(file: File){
        file.createNewFile()
        val encoder = ObjectEncoder()
        file.writeBytes(encoder.encode(blockChain))
    }
    fun loadBlockChain(file: File){
        var encoder = ObjectEncoder()
        val obj: BlockChain = encoder.decode(file.readBytes())
        this.blockChain = obj
    }
    fun onNewTransaction(transaction: Transaction){
        println("GOT NEW TRANSACTION: $transaction")
        if(isTransactionValid(transaction)) {
            blockChain.pendingTransactions.add(transaction)
        }
    }
    private fun isTransactionValid(transaction: Transaction): Boolean{
        if (transaction is MoneyTransaction){
            if (transaction.senderAddress != null) {
                // CHECK IF SENDER HAS ENOUGH BALANCE
                val balanceOfSender = getBalanceOfAddress(transaction.senderAddress.address)
                if (balanceOfSender >= transaction.amount){

                    val publicKey: PublicKey = transaction.senderAddress.publicKey
                    return transaction.verify(publicKey)
                }
            }
        } else return true
        return false
    }
    fun getBalanceOfAddress(address: String, debug: Boolean = false): Float{
        var balance = 0f
        for (block in blockChain.chain){
            for (transaction in block.transactions + block.coinBaseTransaction){
                if (transaction is CoinBaseTransaction){
                    if (transaction.recipientAddress.address == address){
                        if (debug) println("BALANCE CHECK: address $address got +${transaction.amount} SRX FROM COINBASETRANSACTION")
                        balance+=transaction.amount
                    }
                }
                if (transaction is MoneyTransaction){
                    if (transaction.recipientAddress == address){
                        if (debug) println("BALANCE CHECK: address $address got +${transaction.amount} SRX FROM RECEIVING MONEY FROM ${transaction.senderAddress?.address}")
                        balance+=transaction.amount
                    } else if (transaction.senderAddress!= null && transaction.senderAddress.address == address){
                        if (debug) println("BALANCE CHECK: address $address got -${transaction.amount} SRX FROM SENDING MONEY TO ${transaction.senderAddress.address}")
                        balance-=transaction.amount
                    }
                }
                if (transaction is DebugTransaction){
                    if (transaction.recipientAddress.address == address){
                        if (debug) println("BALANCE CHECK: address $address got +${transaction.amount} SRX FROM DEBUG TRANSACTION")
                        balance+=transaction.amount
                    }
                }
            }
        }
        return balance
    }
    fun getPublicKeyByAddress(address: String): PublicKey?{
        for (block in blockChain.chain) {
            for (transaction in block.transactions){
                if (transaction is AddressTransaction){
                    if (transaction.address == address){
                        return transaction.publicKey
                    }
                }
            }
        }
        return null
    }
    fun startMineLoop(){
        Thread{
            for (i in 0..5){
                Thread.sleep(100)
                val newBlock = blockChain.mineOneBlock(miningWallet.addresses[0])
                if (newBlock != null){
                    onNewBlock(newBlock)
                    val encoder = ObjectEncoder()
                    val blockData = encoder.encode(newBlock)
                    // TODO("BROADCAST THE BLOCK DATA")
                }
            }
        }.start()
    }
}
