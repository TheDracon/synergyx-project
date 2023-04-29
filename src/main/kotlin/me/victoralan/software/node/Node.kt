package me.victoralan.software.node

import me.victoralan.blockchain.Hash
import me.victoralan.blockchain.Block
import me.victoralan.blockchain.BlockChain
import me.victoralan.blockchain.transactions.*
import me.victoralan.crypto.encoder.ObjectEncoder
import me.victoralan.software.node.networking.NodeServer
import me.victoralan.software.wallet.Address
import me.victoralan.software.wallet.Wallet
import me.victoralan.utils.Validator
import java.io.File
import java.net.InetSocketAddress
import java.security.PublicKey

class Node(walletPassword: String, host: InetSocketAddress = InetSocketAddress("localhost", 19101)) {
    var currentBlockMiningHash: Hash = Hash.empty()
    var blockChain: BlockChain = BlockChain(difficulty = 5, blockSize = 4, minimumBlockValidity = 0)
    var miningWallet: Wallet
    private var addressCache: ArrayList<Address> = ArrayList()
    val nodeServer: NodeServer = NodeServer(host, this)
    private var miningAll: Boolean = false
    var neighbours: ArrayList<InetSocketAddress> = ArrayList()
    init {
        miningWallet = Wallet(password = walletPassword, _nodeAddress = host)
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
            if (length > maxLength) {
                if (blockChain.isValid()) {
                    maxLength = length
                    newChain = blockChain
                }
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
        if (blockChain.pendingTransactions.any {pendingTrans -> pendingTrans.hash.toString() == transaction.hash.toString()}) return 0
        // If transaction already in blockChain then we can skip
        if (blockChain.chain.any { block -> block.transactions.any { transactionInBlock -> transactionInBlock.hash == transaction.hash } }) return 0
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
        if (blockChain.chain.any { block2 -> block2.hash.toString() == block.hash.toString() }) {
            return false
        }
        if (Validator.isBlockValid(this, block, false)){
            // ADD THE BLOCK
            blockChain.addBlock(block)

            nodeServer.broadcastNewBlock(neighbours, block)
            //TODO("CHECK THAT BROADCASTING WORKS")
            block.transactions.forEach {transaction ->
                blockChain.pendingTransactions.removeAll {
                    pendingTransaction -> transaction.hash.toString() == pendingTransaction.hash.toString() && blockChain.pendingTransactions.any {it.hash.toString() == pendingTransaction.hash.toString()}
                }
            }
            return true
        }
        return false
    }
    fun start(){
        this.nodeServer.startServer()
        miningWallet.createAddress(0)
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
            if ((block.index - blockChain.chain.size) == blockChain.minimumBlockValidity) {
                continue
            }
            for (transaction in block.transactions + block.coinBaseTransaction){
                if (transaction is CoinBaseTransaction){
                    if (transaction.recipientAddress.address == address){
                        balance+=transaction.amount
                    }
                }
                if (transaction is MoneyTransaction){
                    if (transaction.recipientAddress == address){
                        balance+=transaction.amount
                    } else if (transaction.senderAddress!= null && transaction.senderAddress.address == address){
                        balance-=transaction.amount
                    }
                }
                if (transaction is DebugTransaction){
                    if (!debug) continue
                    if (transaction.recipientAddress.address == address){
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
                        return transaction.address.publicKey
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

    companion object{
        fun getBalanceOfAddress(address: String, blockChain: BlockChain, debug: Boolean = false): Float {
            var balance = 0f
            for (block in blockChain.chain) {
                // IF BLOCK HAS NOT ENOUGH BLOCKS IN FRONT IT DOESN'T COUNT FOR BALANCE
                if ((block.index - blockChain.chain.size) == blockChain.minimumBlockValidity) {
                    continue
                }
                for (transaction in block.transactions + block.coinBaseTransaction) {
                    if (transaction is CoinBaseTransaction) {
                        if (transaction.recipientAddress.address == address) {
                            balance += transaction.amount
                        }
                    }
                    if (transaction is MoneyTransaction) {
                        if (transaction.recipientAddress == address) {
                            balance += transaction.amount
                        } else if (transaction.senderAddress != null && transaction.senderAddress.address == address) {
                            balance -= transaction.amount
                        }
                    }
                    if (transaction is DebugTransaction) {
                        if (!debug) continue
                        if (transaction.recipientAddress.address == address) {
                            balance += transaction.amount
                        }
                    }
                }
            }
            return balance
        }
    }
}
