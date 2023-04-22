package me.victoralan

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import me.victoralan.crypto.SHA3
import java.math.BigInteger

@JsonSerialize(using = HashSerializer::class)
class Hash {

    val value: ByteArray

    constructor(hashValue: ByteArray) {
        value = hashValue
    }
    constructor(hashValue: String) {
        value = hashValue.toByteArray()
    }


    fun binaryString(): String{
        return value.joinToString("") { byte ->
            String.format("%8s", byte.toInt().and(0xFF).toString(2)).replace(' ', '0')
        }
    }

    override fun toString(): String {
        val str = BigInteger(1, value).toString(16)
        return str
    }


    companion object{
        /**
         * Gets a Hash object from the hash of the string
         * @param string The string to hash
         * @return The new Hash object
         */
        fun fromString(string: String): Hash{
            return Hash(SHA3.hashString(string))
        }
        fun empty() : Hash{
            return Hash("")
        }
        fun getBinaryString(value: ByteArray): String{
            return value.joinToString("") { byte ->
                String.format("%8s", byte.toInt().and(0xFF).toString(2)).replace(' ', '0')
            }
        }
    }
}

class HashSerializer : JsonSerializer<Hash>() {
    override fun serialize(hash: Hash, jsonGenerator: JsonGenerator, serializerProvider: SerializerProvider) {
        jsonGenerator.writeString(hash.toString())
    }
}