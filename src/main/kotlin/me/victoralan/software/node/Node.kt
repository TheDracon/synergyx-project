package me.victoralan.software.node

import me.victoralan.Hash
import me.victoralan.blockchain.Block
import me.victoralan.blockchain.BlockChain
import me.victoralan.blockchain.transactions.*
import me.victoralan.crypto.encoder.ObjectEncoder
import me.victoralan.software.node.networking.NodeServer
import me.victoralan.software.wallet.Address
import me.victoralan.software.wallet.Wallet
import java.io.File
import java.net.InetAddress
import java.net.InetSocketAddress
import java.security.PublicKey

class Node(walletPassword: String) {
    var currentBlockMiningHash: Hash = Hash.empty()
    var blockChain: BlockChain = BlockChain(difficulty = 20, blockSize = 2)
    var miningWallet: Wallet
    private var addressCache: ArrayList<Address> = ArrayList()
    private val nodeServer: NodeServer = NodeServer(InetAddress.getLocalHost(), 8081, this)
    private var miningAll: Boolean = false
    var neighbours: ArrayList<InetSocketAddress> = ArrayList()
    init {
        miningWallet = Wallet(password = walletPassword)
    }

    /**
     * @return Whether the chain has been replaced or not
     */
    fun syncNodes(): Boolean {
        var newChain: BlockChain? = null
        var maxLength = blockChain.chain.size

        for (node in neighbours){
            val response = nodeServer.getBlockChainOf(node, maxLength) ?: continue
            val length = response.first
            val blockChain = ObjectEncoder().decode<BlockChain>(response.second)
            if (length > maxLength && blockChain.isValid(this)){
                maxLength = length
                newChain = blockChain
            }
        }
        if (newChain != null){
            blockChain = newChain
            return true
        }
        return false
    }

    /**
     * @param transaction The transaction to check
     * @return An integer with the validity transaction: If 0 then the transaction is valid, if 1 then there is no senderAddress, if 2 then sender has not enough money to send transaction, if 3 then transaction is not or incorrectly singed
     */
    fun onNewTransaction(transaction: Transaction): Int {
        // If transaction already in pendingTransactions then we can skip
        if (blockChain.pendingTransactions.any {pendingTrans -> pendingTrans.hash == transaction.hash}) return 0
        // If transaction already in blockChain then we can skip

        if (blockChain.chain.any { block -> block.transactions.any { transactionInBlock -> transactionInBlock.hash == transaction.hash } }) return 0
        println("GOT NEW TRANSACTION: $transaction")
        val validity = Validator.isTransactionValid(transaction, this)
        if(validity == 0) {
            blockChain.pendingTransactions.add(transaction)
            nodeServer.broadcastNewTransaction(neighbours, transaction)
            //TODO("CHECK IF BROADCASTING WORKS")
            return 0
        }
        return validity
    }
    fun onNewBlock(block: Block): Boolean {
        // If block already in blockchain then we can skip this
        if (blockChain.chain.any { block2 -> block2.hash == block.hash }) return true

        if (Validator.isBlockValid(this, block)){
            // ADD THE BLOCK
            blockChain.addBlock(block)
            nodeServer.broadcastNewBlock(neighbours, block)
            //TODO("CHECK THAT BROADCASTING WORKS")
            println(block.transactions.size)
            for (transaction in block.transactions){
                if (transaction is AddressTransaction){
                    addressCache.add(transaction.address)
                }
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


    fun saveBlockChain(file: File){
        file.createNewFile()
        val encoder = ObjectEncoder()
        file.writeBytes(encoder.encode(blockChain))
    }
    fun loadBlockChain(file: File){
        val encoder = ObjectEncoder()
        val obj: BlockChain = encoder.decode(file.readBytes())
        this.blockChain = obj
    }




    fun searchTransactionByHash(hash: Hash): Transaction?{
        for (block in blockChain.chain){
            for (transaction in block.transactions){
                if (transaction.hash == hash){
                    return transaction
                }
            }
        }
        return null
    }
    fun getBalanceOfAddress(address: String, debug: Boolean = false): Float{
        var balance = 0f
        for (block in blockChain.chain){
            // IF BLOCK HAS NOT ENOUGH BLOCKS IN FRONT IT DOESN'T COUNT FOR BALANCE
            if (!Validator.isBlockFullyValid(this, block)) continue
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
                    if (transaction.address.address == address){
                        return transaction.publicKey
                    }
                }
            }
        }
        return null
    }
    fun mineAvailable(miningAddress: Address){
        Thread{
            this.miningAll = true
            while (miningAll){
                val newBlock = blockChain.mineOneBlock(miningAddress, this)
                if (newBlock != null) onNewBlock(newBlock)

            }
        }.start()
    }
    fun stopMining(){
        this.miningAll = false
        this.currentBlockMiningHash = Hash.empty()
    }
}
