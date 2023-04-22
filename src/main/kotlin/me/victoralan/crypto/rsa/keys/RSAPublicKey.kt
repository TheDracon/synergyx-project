package rsa.keys

import rsa.utils.ArrayUtils.Companion.removeSubarrayFromArray
import rsa.utils.ArrayUtils.Companion.split
import java.lang.RuntimeException
import java.math.BigInteger
import java.util.*

class RSAPublicKey(val n: BigInteger, val e: BigInteger) : RSAKey() {
    override fun getEncoded(): ByteArray {
        return Base64.getEncoder().encode(prefix.toByteArray() + n.toByteArray() + defaultSplitter + e.toByteArray())
    }
    override fun toString(): String {
        return "RSAPublicKey(modulus=$n, publicExponent=$e)"
    }
    companion object{
        private val defaultSplitter = "\n\n\n\n".toByteArray()
        const val prefix = "RSAPublicKey: "
        fun fromEncoded(byteArray: ByteArray): RSAPublicKey {
            var decoded = Base64.getDecoder().decode(byteArray)
            if (!decoded.decodeToString().startsWith(prefix)) {
                throw RuntimeException("Not a public key: ${decoded.decodeToString()}")
            }
            decoded = decoded.removeSubarrayFromArray(prefix.toByteArray())

            val split: List<ByteArray> = decoded.split(defaultSplitter)
            if (split.size != 2) {
                throw RuntimeException("Invalid encoded byteArray, size was ${split.size}")
            }
            val modulus = BigInteger(1, split[0])
            val publicExponent = BigInteger(1, split[1])

            return RSAPublicKey(modulus, publicExponent)
        }



    }
}