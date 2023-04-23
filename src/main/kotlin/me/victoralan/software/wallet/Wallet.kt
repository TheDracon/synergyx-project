package me.victoralan.software.wallet

import me.victoralan.blockchain.blockitems.BlockItem
import me.victoralan.blockchain.blockitems.Transaction
import me.victoralan.crypto.Base58
import me.victoralan.crypto.ecdsa.ECDSA
import me.victoralan.software.Node
import net.glxn.qrgen.javase.QRCode
import java.security.KeyPair
import java.security.MessageDigest
import java.security.SecureRandom

class Wallet(val keyPair: KeyPair) {
    val transactions: List<Transaction> = ArrayList()
    var currentValance: Float = 0f
    var passphrase: String = ""
    var userSettings: UserSettings = UserSettings()
    var trustedNodes: List<Node> = ArrayList()
    var pendingBlockItems: List<BlockItem> = ArrayList()
    var qrCodes: ArrayList<QRCode> = ArrayList()
    // TODO("ADDRESS BOOK")

    fun getAddress(): String {
        val hash = MessageDigest.getInstance("SHA-256").digest(keyPair.public.encoded)
        return hash.joinToString(separator = "") { "%02x".format(it) }
    }
    fun generateAddress(version: Int): String {
        // Step 1: Perform SHA-256 hash on the public key
        val sha256 = MessageDigest.getInstance("SHA-256")
        val hash1 = sha256.digest(keyPair.public.encoded)

        // Step 2: Perform SHA3-256 hash on the result of Step 1
        val sha3256 = MessageDigest.getInstance("SHA3-256")
        val hash2 = sha3256.digest(hash1)

        // Step 3: Add version byte to the front of the result of Step 2
        val versionByte = byteArrayOf((version-128).toByte(), SecureRandom(version.toBigInteger().toByteArray()).nextInt().toByte())
        val hash3 = ByteArray(versionByte.size + hash2.size)
        System.arraycopy(versionByte, 0, hash3, 0, versionByte.size)
        System.arraycopy(hash2, 0, hash3, versionByte.size, hash2.size)

        // Step 4: Perform SHA-256 hash on the result of Step 3
        val hash4 = sha256.digest(hash3)

        // Step 5: Perform SHA-256 hash on the result of Step 4
        val hash5 = sha256.digest(hash4)

        // Step 6: Take the first 4 bytes of the result of Step 5, and add them to the end of the result of Step 3
        val checksum = ByteArray(4)
        System.arraycopy(hash5, 0, checksum, 0, 4)
        val hash6 = ByteArray(hash3.size + checksum.size)
        System.arraycopy(hash3, 0, hash6, 0, hash3.size)
        System.arraycopy(checksum, 0, hash6, hash3.size, checksum.size)

        // Step 7: Encode the result of Step 6 using Base58Check encoding
        return Base58.encode(hash6)
    }
    fun signTransaction(transaction: ByteArray): ByteArray {
        return ECDSA().signMessage(transaction, keyPair.private)
    }

    fun verifySignature(transaction: ByteArray, signature: ByteArray): Boolean {
        return ECDSA().verifySignature(transaction, signature, keyPair.public)
    }


}
