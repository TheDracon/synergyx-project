import rsa.keys.RSAPrivateKey
import java.math.BigInteger
import java.util.*

class RSADecryptor(private val rsaPrivateKey: RSAPrivateKey) {


    fun decryptBytes(encryptedMessage: ByteArray) : ByteArray{
        val messageAsBigInteger = BigInteger(1, (encryptedMessage))

        val decryptedMessage: BigInteger = messageAsBigInteger.modPow(rsaPrivateKey.d, rsaPrivateKey.n)

        return decryptedMessage.toByteArray()
    }

    fun decryptBigInteger(encryptedMessage: BigInteger): ByteArray{
        val decryptedMessage: BigInteger = encryptedMessage.modPow(rsaPrivateKey.d, rsaPrivateKey.n)
        return decryptedMessage.toByteArray()
    }
    fun decryptString(message: String) : String{
        val messageAsBigInteger = BigInteger(1, (Base64.getDecoder().decode(message.toByteArray())))
        val encryptedMessage: BigInteger = messageAsBigInteger.modPow(rsaPrivateKey.d, rsaPrivateKey.n)
        return encryptedMessage.toByteArray().decodeToString()
    }

}