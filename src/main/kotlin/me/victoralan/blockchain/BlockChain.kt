package me.victoralan.blockchain

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.ObjectMapper
import me.victoralan.blockchain.transactions.CoinBaseTransaction
import me.victoralan.blockchain.transactions.Transaction
import me.victoralan.software.node.Node
import me.victoralan.utils.Validator
import me.victoralan.software.wallet.Address
import java.io.Serializable

class BlockChain(val difficulty: Int = 5, val reward: Float = 1f, val blockSize: Int = 10, val minimumBlockValidity: Int = 5) : Serializable {

    var chain: ArrayList<Block> = ArrayList()
    var pendingTransactions: ArrayList<Transaction> = ArrayList()

    init {
        addGenesisBlock()
    }
    fun mineOneBlock(miner: Address, node: Node): Block?{

        if (pendingTransactions.size >= blockSize) {

            val end = blockSize
            val transactionSlice: ArrayList<Transaction> = pendingTransactions.toArray().copyOfRange(0, end)
                .toCollection(ArrayList()) as ArrayList<Transaction>

            val newBlock = Block(transactionSlice, System.nanoTime(), chain.size)
            newBlock.coinBaseTransaction = CoinBaseTransaction(miner, reward)

            val hashVal = getLastBlock().hash
            newBlock.previousBlockHash = hashVal
            node.currentBlockMiningHash = newBlock.hash
            if ( newBlock.mine(difficulty) ) {
                println("Block mined, nonce to solve PoW: ${newBlock.nonce}")
                return newBlock
            }
        }
        return null
    }
    fun isValid(): Boolean {
        for (block in chain){
            if (Validator.isBlockValid(this, block)){
                for (transaction in block.transactions){
                    val validity = Validator.isTransactionValid(transaction, this)
                    if (validity != 0) {
                        return false
                    }
                }
            } else {
                return false
            }
        }
        return true
    }
    fun addTransaction(bObject: Transaction){
        pendingTransactions.add(bObject)
    }
    /**
     * Adds a block to the chain, if the is no previous block then the hash will be an empty string.
     * @param newBlock The new block to add to the chain
     * @return if the block was successfully added
     */
    fun addBlock(newBlock: Block): Boolean{
        if (chain.size > 0 ){
            newBlock.previousBlockHash = getLastBlock().hash
        }
        chain.add(newBlock)
        return true
    }
    @JsonIgnore
    fun getLastBlock(): Block {
        return chain[chain.size-1]
    }
    /**
     * Adds the first block that is just empty
     */
    private fun addGenesisBlock(){
        val transactions = ArrayList<Transaction>()
        val genesisBlock = Block(transactions, System.nanoTime(), 0)
        genesisBlock.previousBlockHash = Hash.empty()
        chain.add(genesisBlock)
    }

    override fun toString(): String {
        val objWrapper = ObjectMapper()
        return objWrapper.writerWithDefaultPrettyPrinter().writeValueAsString(this)
    }
}