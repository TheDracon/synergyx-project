package me.victoralan.crypto.encoder

import java.io.UnsupportedEncodingException
import java.math.BigInteger
import java.nio.charset.Charset
import java.security.MessageDigest
import java.util.*


object Base58 {
    val ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz".toCharArray()
    private val INDEXES = IntArray(128)

    init {
        for (i in INDEXES.indices) {
            INDEXES[i] = -1
        }
        for (i in ALPHABET.indices) {
            INDEXES[ALPHABET[i].code] = i
        }
    }

    /** Encodes the given bytes in base58. No checksum is appended.  */
    fun encode(inp: ByteArray): String {
        var input = inp
        if (input.isEmpty()) {
            return ""
        }
        input = copyOfRange(input, 0, input.size)
        // Count leading zeroes.
        var zeroCount = 0
        while (zeroCount < input.size && input[zeroCount].toInt() == 0) {
            ++zeroCount
        }
        // The actual encoding.
        val temp = ByteArray(input.size * 2)
        var j = temp.size
        var startAt = zeroCount
        while (startAt < input.size) {
            val mod = divmod58(input, startAt)
            if (input[startAt].toInt() == 0) {
                ++startAt
            }
            temp[--j] = ALPHABET[mod.toInt()].code.toByte()
        }

        // Strip extra '1' if there are some after decoding.
        while (j < temp.size && temp[j] == ALPHABET[0].code.toByte()) {
            ++j
        }
        // Add as many leading '1' as there were leading zeros.
        while (--zeroCount >= 0) {
            temp[--j] = ALPHABET[0].code.toByte()
        }
        val output = copyOfRange(temp, j, temp.size)
        return try {
            String(output, Charset.defaultCharset())
        } catch (e: UnsupportedEncodingException) {
            throw RuntimeException(e) // Cannot happen.
        }
    }

    fun decode(input: String): ByteArray {
        if (input.length == 0) {
            return ByteArray(0)
        }
        val input58 = ByteArray(input.length)
        // Transform the String to a base58 byte sequence
        for (i in 0 until input.length) {
            val c = input[i]
            var digit58 = -1
            if (c.code >= 0 && c.code < 128) {
                digit58 = INDEXES[c.code]
            }
            if (digit58 < 0) {
                throw java.lang.RuntimeException("Illegal character $c at $i")
            }
            input58[i] = digit58.toByte()
        }
        // Count leading zeroes
        var zeroCount = 0
        while (zeroCount < input58.size && input58[zeroCount].toInt() == 0) {
            ++zeroCount
        }
        // The encoding
        val temp = ByteArray(input.length)
        var j = temp.size
        var startAt = zeroCount
        while (startAt < input58.size) {
            val mod = divmod256(input58, startAt)
            if (input58[startAt].toInt() == 0) {
                ++startAt
            }
            temp[--j] = mod
        }
        // Do no add extra leading zeroes, move j to first non null byte.
        while (j < temp.size && temp[j].toInt() == 0) {
            ++j
        }
        return copyOfRange(temp, j - zeroCount, temp.size)
    }

    fun decodeToBigInteger(input: String): BigInteger {
        return BigInteger(1, decode(input))
    }

    /**
     * Uses the checksum in the last 4 bytes of the decoded data to verify the rest are correct. The checksum is
     * removed from the returned data.
     *
     * @throws AddressFormatException if the input is not base 58 or the checksum does not validate.
     */
    fun decodeChecked(input: String): ByteArray {
        var tmp = decode(input)
        if (tmp.size < 4) throw java.lang.RuntimeException("Input to short")
        val bytes = copyOfRange(tmp, 0, tmp.size - 4)
        val checksum = copyOfRange(tmp, tmp.size - 4, tmp.size)
        tmp = doubleDigest(bytes)
        val hash = copyOfRange(tmp, 0, 4)
        if (!Arrays.equals(checksum, hash)) throw RuntimeException("Checksum does not validate")
        return bytes
    }

    fun doubleDigest(input: ByteArray): ByteArray {
        return doubleDigest(input, 0, input.size)
    }
    fun doubleDigest(input: ByteArray?, offset: Int, length: Int): ByteArray {
        val digest: MessageDigest = MessageDigest.getInstance("SHA-256")
        synchronized(digest) {
            digest.reset()
            digest.update(input, offset, length)
            val first: ByteArray = digest.digest()
            return digest.digest(first)
        }
    }
    //
    // number -> number / 58, returns number % 58
    //
    private fun divmod58(number: ByteArray, startAt: Int): Byte {
        var remainder = 0
        for (i in startAt until number.size) {
            val digit256 = number[i].toInt() and 0xFF
            val temp = remainder * 256 + digit256
            number[i] = (temp / 58).toByte()
            remainder = temp % 58
        }
        return remainder.toByte()
    }

    //
    // number -> number / 256, returns number % 256
    //
    private fun divmod256(number58: ByteArray, startAt: Int): Byte {
        var remainder = 0
        for (i in startAt until number58.size) {
            val digit58 = number58[i].toInt() and 0xFF
            val temp = remainder * 58 + digit58
            number58[i] = (temp / 256).toByte()
            remainder = temp % 256
        }
        return remainder.toByte()
    }

    private fun copyOfRange(source: ByteArray, from: Int, to: Int): ByteArray {
        val range = ByteArray(to - from)
        System.arraycopy(source, from, range, 0, range.size)
        return range
    }
}