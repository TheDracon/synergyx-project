package me.victoralan.blockchain

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import me.victoralan.Hash
import me.victoralan.blockchain.blockitems.BlockItem
import me.victoralan.blockchain.blockitems.Transaction
import me.victoralan.entities.User

class BlockChain(val difficulty: Int = 5, val reward: Float = 1f, val blockSize: Int = 1) {

    @JsonProperty
    var chain: ArrayList<Block> = ArrayList()
    var pendingBlockItems: ArrayList<BlockItem> = ArrayList()

    init {
        addGenesisBlock()
    }
        fun minePendingBlockItems(miner: User){

            if (pendingBlockItems.size >= blockSize) {
                for (i in 0..pendingBlockItems.size step blockSize) {
                    println(i)
                    var end = i + blockSize
                    if (i >= pendingBlockItems.size) {
                        end = pendingBlockItems.size
                    }
                    val transactionSlice: ArrayList<BlockItem> = pendingBlockItems.toArray().copyOfRange(i, end)
                        .toCollection(ArrayList()) as ArrayList<BlockItem>

                    val newBlock = Block(transactionSlice, System.nanoTime(), chain.size.toLong())

                    val hashVal = getLastBlock().hash
                    newBlock.previousBlockHash = hashVal
                    newBlock.mine(difficulty)
                    chain.add(newBlock)
                }
                println("Mining BlockItems Success!")

                val rewardTransaction = Transaction(User("rewards"), miner, reward)
                pendingBlockItems.add(rewardTransaction)
            } else{
                println("Not inof transactions to mine, need at least $blockSize and have ${pendingBlockItems.size}")
            }
        }
    fun addBlockItem(bObject: BlockItem){
        pendingBlockItems.add(bObject)
    }
    /**
     * Adds a block to the chain, if the is no previous block then the hash will be an empty string.
     * @param newBlock The new block to add to the cchain
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
        val transactions = ArrayList<BlockItem>()
        val genesisBlock = Block(transactions, System.nanoTime(), 0)
        genesisBlock.previousBlockHash = Hash.empty()
        chain.add(genesisBlock)
    }

    override fun toString(): String {
        val objWrapper = ObjectMapper()
        return objWrapper.writerWithDefaultPrettyPrinter().writeValueAsString(this)
    }
}