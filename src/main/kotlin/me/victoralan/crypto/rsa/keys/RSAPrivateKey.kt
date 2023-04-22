package rsa.keys

import rsa.utils.ArrayUtils.Companion.removeSubarrayFromArray
import rsa.utils.ArrayUtils.Companion.split
import java.lang.RuntimeException
import java.math.BigInteger
import java.util.*

class RSAPrivateKey(val n: BigInteger, val d: BigInteger) : RSAKey() {
    override fun getEncoded(): ByteArray {
        return Base64.getEncoder().encode(prefix.toByteArray() + n.toByteArray() + defaultSplitter + d.toByteArray())
    }
    override fun toString(): String {
        return "RSAPrivateKey(modulus=$n, publicExponent=$d)"
    }
    companion object{
        private val defaultSplitter = "\n\n\n\n".toByteArray()
        const val prefix = "RSAPrivateKey: "
        fun fromEncoded(byteArray: ByteArray): RSAPrivateKey {
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
            val privateExponent = BigInteger(1, split[1])

            return RSAPrivateKey(modulus, privateExponent)
        }

    }

}