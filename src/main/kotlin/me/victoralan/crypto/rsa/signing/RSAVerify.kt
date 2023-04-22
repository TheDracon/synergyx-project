package rsa.signing

import rsa.keys.RSAPrivateKey
import rsa.keys.RSAPublicKey
import sha.SHA3
import java.math.BigInteger
import java.security.MessageDigest
import java.util.*

class RSAVerify(private val rsaPublicKey: RSAPublicKey) {

    fun readBytes(encryptedMessage: ByteArray) : ByteArray{
        val messageAsBigInteger = BigInteger(1, Base64.getDecoder().decode(encryptedMessage))
        val decryptedMessage: BigInteger = messageAsBigInteger.modPow(rsaPublicKey.e, rsaPublicKey.n)

        return decryptedMessage.toByteArray()
    }
    fun readString(message: String) : String{
        val messageAsBigInteger = BigInteger(1, Base64.getDecoder().decode(message.toByteArray()))
        val encryptedMessage: BigInteger = messageAsBigInteger.modPow(rsaPublicKey.e, rsaPublicKey.n)
        return encryptedMessage.toByteArray().decodeToString()
    }

    fun verify(message: String, originalMessage: String) : Boolean{
        return readString(message) == SHA3.sha3(originalMessage.toByteArray()).toByteArray().decodeToString()
    }
    fun verify(message: ByteArray, originalMessage: ByteArray) : Boolean{
        return readBytes(message).contentEquals(SHA3.sha3(originalMessage).toByteArray())

    }
}