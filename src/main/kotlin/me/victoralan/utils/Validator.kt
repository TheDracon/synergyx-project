package me.victoralan.utils

import me.victoralan.blockchain.Block
import me.victoralan.blockchain.BlockChain
import me.victoralan.blockchain.transactions.CoinBaseTransaction
import me.victoralan.blockchain.transactions.DebugTransaction
import me.victoralan.blockchain.transactions.MoneyTransaction
import me.victoralan.blockchain.transactions.Transaction
import me.victoralan.software.node.Node
import java.security.PublicKey

class Validator {
    companion object {
        /**
         * Check if a block is valid
         * @param node The node to get all information about blockchain
         * @return Whether the block is valid or not
         */
        fun isBlockValid(blockChain: BlockChain, block: Block, isInBlockChain: Boolean = true): Boolean {
            if (blockChain.chain.size <= 1) return true

            if (isInBlockChain) {
                try {
                    if (blockChain.chain[block.index] != block) return false
                } catch (e: ArrayIndexOutOfBoundsException) {
                    return false
                }
            }

            if (block.isGenesis()) return true
            val previousBlock = blockChain.chain[block.index - 1]
            if (block.transactions.filterIsInstance<DebugTransaction>().isNotEmpty()) {
                return true
            }
            //CHECK IF BLOCK IS FULL
            if (block.transactions.size == blockChain.blockSize) {

                //CHECK IF CALCULATION OF HASH IS THE SAME AS THE HASH
                if (block.hash.value.contentEquals(block.calculateHash().value)) {

                    //CHECK IF BLOCK HASH STARTS WITH difficulty AMOUNT OF 0's
                    if (block.hash.binaryString().startsWith("0".repeat(blockChain.difficulty))) {

                        //CHECK IF HASH OF PREVIOUS BLOCK IS VALID
                        if (isInBlockChain) if (!block.previousBlockHash.value.contentEquals(previousBlock.hash.value)) return false
                        if (block.coinBaseTransaction == null) return false
                        if (block.coinBaseTransaction!!.amount != blockChain.reward) return false
                        //CHECK IF TRANSACTIONS OF BLOCK ARE VALID
                        for (transaction in block.transactions) {
                            if (isTransactionValid(transaction, blockChain) != 0) {
                                return false
                            }
                        }
                        return true

                    }
                }
            }
            return false
        }

        fun isBlockValid(node: Node, block: Block, isInBlockChain: Boolean = true): Boolean {
            return isBlockValid(node.blockChain, block, isInBlockChain)
        }

        fun isBlockFullyValid(node: Node, block: Block, isInBlockChain: Boolean = true): Boolean {
            if (isBlockValid(node, block, isInBlockChain)) {
                if (node.blockChain.chain.indexOf(block) == block.index) {
                    if (node.blockChain.chain.contains(block)) {
                        val blocksAfterBlock = node.blockChain.chain.size - node.blockChain.chain.indexOf(block)
                        return blocksAfterBlock > node.blockChain.minimumBlockValidity
                    }
                }
            }
            return false
        }

        /**
         * Check if a transaction is valid
         * @param transaction The transaction to check
         * @return An integer with the validity transaction: If 0 then the transaction is valid, if 1 then there is no senderAddress, if 2 then sender has not enough money to send transaction, if 3 then transaction is not or incorrectly singed
         */

        fun isTransactionValid(transaction: Transaction, blockChain: BlockChain): Int {
            if (transaction is MoneyTransaction) {
                if (transaction.senderAddress != null) {
                    // CHECK IF SENDER HAS ENOUGH BALANCE
                    val balanceOfSender = Node.getBalanceOfAddress(transaction.senderAddress.address, blockChain, true) //TODO("NOT DEBUG MODE")
                    if (balanceOfSender >= transaction.amount) {
                        val publicKey: PublicKey = transaction.senderAddress.publicKey
                        return if (transaction.verify(publicKey)) 0 else 3
                    } else return 2
                } else return 1
            } else return 0
        }

        fun isTransactionValid(transaction: Transaction, node: Node): Int {
            return isTransactionValid(transaction, node.blockChain)
        }
    }
}