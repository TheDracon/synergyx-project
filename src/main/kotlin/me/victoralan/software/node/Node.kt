package me.victoralan.software.node

import me.victoralan.blockchain.Block
import me.victoralan.blockchain.BlockChain
import me.victoralan.blockchain.blockitems.AddressTransaction
import me.victoralan.blockchain.blockitems.MoneyTransaction
import me.victoralan.blockchain.blockitems.RewardTransaction
import me.victoralan.blockchain.blockitems.Transaction
import me.victoralan.crypto.encoder.Base58
import me.victoralan.crypto.encoder.ObjectEncoder
import me.victoralan.software.wallet.Address
import me.victoralan.software.wallet.Wallet
import java.security.PublicKey

class Node(walletPassword: String) {
    val blockChain: BlockChain = BlockChain(difficulty = 23, blockSize = 2)
    var miningWallet: Wallet
    var addressCache: List<Address> = ArrayList()
    init {
        miningWallet = Wallet(password = walletPassword)
    }
    fun onNewBlock(block: Block): Boolean{
        println("NEW BLOCK!")
        println("test1")

        if (isBlockValid(block)){
            println("test7")
            for (transaction in block.transactions){
                for (pendingTransaction in blockChain.pendingTransactions.clone() as ArrayList<Transaction>){
                    if (transaction.hash == pendingTransaction.hash){
                        println("IS VALID")

                        //REMOVE FROM PENDING TRANSACTIONS
                        blockChain.pendingTransactions.remove(pendingTransaction)
                    }
                }
            }
        }
        return true
    }
    private fun isBlockValid(block: Block): Boolean{
        //CHECK IF BLOCK IS FULL
        if (block.transactions.size == blockChain.blockSize){
            println("test2")
            //CHECK IF CALCULATION OF HASH IS THE SAME AS THE HASH
            if (block.hash.value.contentEquals(block.calculateHash().value)){
                println("test3")

                //CHECK IF BLOCK HASH STARTS WITH difficulty AMOUNT OF 0's
                if (block.hash.binaryString().startsWith("0".repeat(blockChain.difficulty))){
                    println("test4")

                    //CHECK IF HASH OF PREVIOUS BLOCK IS VALID
                    println("PREVIOUS: ${Base58.encode(blockChain.getLastBlock().hash.value)}")
                    println("ACTUAL: ${Base58.encode(block.previousBlockHash.value)}")

                    if (block.previousBlockHash.value.contentEquals(blockChain.getLastBlock().hash.value)){
                        println("test5")

                        //CHECK IF TRANSACTIONS OF BLOCK ARE VALID
                        for (transaction in block.transactions){
                            if (!isTransactionValid(transaction)){
                                return false
                            }
                        }
                        println("test6")

                        return true
                    }
                }
            }
        }
        return false
    }
    fun onNewTransaction(transaction: Transaction){

        if(isTransactionValid(transaction)){
            println("IS VALID")
            blockChain.pendingTransactions.add(transaction)
        } else{
            println("IS NOT VALID")

        }


    }
    private fun isTransactionValid(transaction: Transaction): Boolean{
        if (transaction is MoneyTransaction){
            if (transaction.senderAddress != null) {
                // CHECK IF SENDER HAS ENOUGH BALANCE
                var balanceOfSender = getBalanceOfAddress(transaction.senderAddress.address)
                if (balanceOfSender >= transaction.balance){
                    val publicKey: PublicKey = transaction.senderAddress.publicKey
                    return transaction.verify(publicKey)
                }
            }
        } else return true
        return false
    }
    fun getBalanceOfAddress(address: String): Float{
        var balance = 0f
        for (block in blockChain.chain){
            for (transaction in block.transactions + block.coinBaseTransaction){
                if (transaction is RewardTransaction){
                    if (transaction.receiver.address == address){
                        balance+=transaction.amount
                    }
                }
                if (transaction is MoneyTransaction){
                    if (transaction.recipientAddress == address){
                        balance+=transaction.balance
                    } else if (transaction.senderAddress!= null && transaction.senderAddress.address == address){
                        balance-=transaction.balance
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
            while (true){
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