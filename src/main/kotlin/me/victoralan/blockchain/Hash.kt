package me.victoralan.blockchain

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import me.victoralan.crypto.SHA3
import java.io.Serializable
import java.math.BigInteger
import java.security.SecureRandom

@JsonSerialize(using = HashSerializer::class)
class Hash : Serializable{

    var value: ByteArray

    constructor(hashValue: ByteArray) {
        value = hashValue
    }
    constructor(hashValue: String) {
        value = hashValue.toByteArray()
    }

    fun shuffle(){
        this.value = random().value
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
        fun fromString(string: String): Hash {
            return Hash(SHA3.hashString(string))
        }

        /**
         * Returns a semi-random Hash
         * @return The new Hash object
         */
        fun random(): Hash {
            return fromString(SecureRandom().nextLong().toString())
        }
        fun empty() : Hash {
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