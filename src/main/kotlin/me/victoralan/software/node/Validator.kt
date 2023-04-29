package me.victoralan.software.node

import me.victoralan.blockchain.Block
import me.victoralan.blockchain.transactions.DebugTransaction
import me.victoralan.blockchain.transactions.MoneyTransaction
import me.victoralan.blockchain.transactions.Transaction
import me.victoralan.crypto.encoder.Base58
import java.security.PublicKey

class Validator {
    companion object{
        /**
         * Check if a block is valid
         * @param node The node to get all information about blockchain
         * @return Whether the block is valid or not
         */
        fun isBlockValid(node: Node, block: Block): Boolean{
            val previousBlock = node.blockChain.chain[(block.index-1).toInt()]
            //CHECK IF BLOCK IS FULL
            if (block.transactions.filterIsInstance<DebugTransaction>().isNotEmpty()){
                return true
            }
            if (block.transactions.size == node.blockChain.blockSize){
                //CHECK IF CALCULATION OF HASH IS THE SAME AS THE HASH
                if (block.hash.value.contentEquals(block.calculateHash().value)){

                    //CHECK IF BLOCK HASH STARTS WITH difficulty AMOUNT OF 0's
                    if (block.hash.binaryString().startsWith("0".repeat(node.blockChain.difficulty))){

                        //CHECK IF HASH OF PREVIOUS BLOCK IS VALID
                        println("PREVIOUS: ${Base58.encode(previousBlock.hash.value)}")
                        println("ACTUAL: ${Base58.encode(block.previousBlockHash.value)}")

                        if (block.previousBlockHash.value.contentEquals(previousBlock.hash.value)){
                            if (block.coinBaseTransaction == null) return false
                            if (block.coinBaseTransaction!!.amount != node.blockChain.reward) return false
                            //CHECK IF TRANSACTIONS OF BLOCK ARE VALID
                            for (transaction in block.transactions){
                                if (isTransactionValid(transaction, node) != 0){
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
        fun isBlockFullyValid(node: Node, block: Block): Boolean{
            if (isBlockValid(node, block)){
                if (node.blockChain.chain.contains(block)){
                    val blocksAfterBlock = node.blockChain.chain.size - node.blockChain.chain.indexOf(block)
                    return blocksAfterBlock > node.blockChain.minimumBlockValidity
                }
            }
            return false
        }
        /**
         * Check if a transaction is valid
         * @param transaction The transaction to check
         * @return An integer with the validity transaction: If 0 then the transaction is valid, if 1 then there is no senderAddress, if 2 then sender has not enough money to send transaction, if 3 then transaction is not or incorrectly singed
         */
        fun isTransactionValid(transaction: Transaction, node: Node): Int{
            if (transaction is MoneyTransaction){
                if (transaction.senderAddress != null) {
                    // CHECK IF SENDER HAS ENOUGH BALANCE
                    val balanceOfSender = node.getBalanceOfAddress(transaction.senderAddress.address)
                    if (balanceOfSender >= transaction.amount){

                        val publicKey: PublicKey = transaction.senderAddress.publicKey
                        return if (transaction.verify(publicKey)) 0 else 3
                    } else return 2
                } else return 1
            } else return 0
        }
    }

}