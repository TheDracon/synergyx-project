package rsa.signing

import rsa.keys.RSAPrivateKey
import sha.SHA3
import java.math.BigInteger
import java.util.*

class RSASign(private val rsaPrivateKey: RSAPrivateKey) {

    fun sign(message: ByteArray) : ByteArray {
        val hash = SHA3.sha3(message)
        val encryptedMessage: BigInteger = hash.modPow(rsaPrivateKey.d, rsaPrivateKey.n)
        return Base64.getEncoder().encode(encryptedMessage.toByteArray())
    }
    fun sign(message: String) : String {
        val hash = SHA3.sha3(message.toByteArray())
        val encryptedMessage: BigInteger = hash.modPow(rsaPrivateKey.d, rsaPrivateKey.n)
        return Base64.getEncoder().encodeToString(encryptedMessage.toByteArray())
    }


}