package rsa.encryption

import rsa.utils.ArrayUtils
import rsa.keys.RSAPublicKey
import java.math.BigInteger
import java.util.*

class RSAEncryptor(private val rsaPublicKey: RSAPublicKey) {

    fun encryptBytes(message: ByteArray) : ByteArray{
        val messageAsBigInteger: BigInteger = BigInteger(1, (message))
        val encryptedMessage: BigInteger = messageAsBigInteger.modPow(rsaPublicKey.e, rsaPublicKey.n)
        return encryptedMessage.toByteArray()
    }
    fun encryptString(message: String) : String{
        val messageAsBigInteger: BigInteger = BigInteger(1, (message.toByteArray()))
        val encryptedMessage: BigInteger = messageAsBigInteger.modPow(rsaPublicKey.e, rsaPublicKey.n)
        return Base64.getEncoder().encodeToString(encryptedMessage.toByteArray())
    }

    fun encryptBigInteger(message: BigInteger): ByteArray{
        val encryptedMessage: BigInteger = message.modPow(rsaPublicKey.e, rsaPublicKey.n)
        return encryptedMessage.toByteArray()
    }


}