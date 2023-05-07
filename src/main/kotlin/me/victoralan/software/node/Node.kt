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
     * @param blockItem The blockItem to check
     * @return An integer with the validity blockItem: If 0 then the blockItem is valid, if 1 then there is no senderAddress, if 2 then sender has not enough money to send blockItem, if 3 then blockItem is not or incorrectly singed
     */
    fun onNewBlockItem(blockItem: BlockItem): Int {

        // If blockItem already in pendingBlockItems then we can skip
        if (blockChain.pendingBlockItems.any { pendingBlockItem -> pendingBlockItem.hash.toString() == blockItem.hash.toString()}) return 0
        // If blockItem already in blockChain then we can skip
        if (blockChain.chain.any { block -> block.blockItems.any { it.hash == blockItem.hash } }) return 0
        val validity = Validator.isBlockItemValid(blockItem, this)
        if(validity == 0) {
            blockChain.pendingBlockItems.add(blockItem)
            nodeServer.broadcastNewBlockItem(neighbours, blockItem)
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
            block.blockItems.forEach { blockItem ->
                blockChain.pendingBlockItems.removeAll {
                    pendingBlockItem -> blockItem.hash.toString() == pendingBlockItem.hash.toString() && blockChain.pendingBlockItems.any {it.hash.toString() == pendingBlockItem.hash.toString()}
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




    fun searchBlockItemByHash(hash: Hash): BlockItem?{
        return searchBlockItemByHash(hash.value)
    }
    fun searchBlockItemByHash(hash: ByteArray): BlockItem?{
        for (block in blockChain.chain){
            for (blockItem in block.blockItems){
                if (blockItem.hash.value.contentEquals(hash)){
                    return blockItem
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
            for (blockItem in block.blockItems + block.coinBaseTransaction){
                if (blockItem is CoinBaseTransaction){
                    if (blockItem.recipientAddress.address == address){
                        balance+=blockItem.amount
                    }
                }
                if (blockItem is Transaction){
                    if (blockItem.recipientAddress == address){
                        balance+=blockItem.amount
                    } else if (blockItem.senderAddress!= null && blockItem.senderAddress.address == address){
                        balance-=blockItem.amount
                    }
                }
                if (blockItem is DebugBlockItem){
                    if (!debug) continue
                    if (blockItem.recipientAddress.address == address){
                        balance+=blockItem.amount
                    }
                }
            }
        }
        return balance
    }
    fun getPublicKeyByAddress(address: String): PublicKey?{
        for (block in blockChain.chain) {
            for (blockItem in block.blockItems){
                if (blockItem is AddressBlockItem){
                    if (blockItem.address.address == address){
                        return blockItem.address.publicKey
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
                for (blockItem in block.blockItems + block.coinBaseTransaction) {
                    if (blockItem is CoinBaseTransaction) {
                        if (blockItem.recipientAddress.address == address) {
                            balance += blockItem.amount
                        }
                    }
                    if (blockItem is Transaction) {
                        if (blockItem.recipientAddress == address) {
                            balance += blockItem.amount
                        } else if (blockItem.senderAddress != null && blockItem.senderAddress.address == address) {
                            balance -= blockItem.amount
                        }
                    }
                    if (blockItem is DebugBlockItem) {
                        if (!debug) continue
                        if (blockItem.recipientAddress.address == address) {
                            balance += blockItem.amount
                        }
                    }
                }
            }
            return balance
        }
    }
}
