package me.victoralan.crypto

import java.math.BigInteger
import java.security.MessageDigest

class SHA3 {
    companion object{
        val algorithm = "SHA3-256"

        fun hashString(data: String): ByteArray{
            val md = MessageDigest.getInstance(algorithm)
            return md.digest(data.toByteArray())
        }
        fun sha3(input: ByteArray): BigInteger {
            val md = MessageDigest.getInstance(algorithm)
            md.update(input)
            return BigInteger(1, md.digest())
        }
    }
}