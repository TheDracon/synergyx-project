package me.victoralan.crypto

import java.security.MessageDigest

class SHA3 {
    companion object{
        fun hashString(data: String): ByteArray{
            val algorithm = "SHA3-256"
            val md = MessageDigest.getInstance(algorithm)
            return md.digest(data.toByteArray())
        }
    }
}