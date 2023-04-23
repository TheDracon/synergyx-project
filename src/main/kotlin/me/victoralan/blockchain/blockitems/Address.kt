package me.victoralan.blockchain.blockitems

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import me.victoralan.Hash
import me.victoralan.crypto.Base58
import me.victoralan.crypto.SHA3
import java.io.Serializable
import java.security.KeyFactory
import java.security.MessageDigest
import java.security.PublicKey
import java.security.SecureRandom
import java.security.spec.X509EncodedKeySpec

class Address(val publicKey: PublicKey, var address: String, override var time: Long = System.nanoTime()): BlockItem(System.nanoTime()),
    Serializable {
    constructor(publicKey: PublicKey, version: Int) : this(publicKey, "") {
        address = generateAddress(version, publicKey)
    }

    fun generateAddress(version: Int, publicKey: PublicKey): String {
        // Step 1: Perform SHA-256 hash on the public key
        val sha256 = MessageDigest.getInstance("SHA-256")
        val hash1 = sha256.digest(publicKey.encoded)

        // Step 2: Perform SHA3-256 hash on the result of Step 1
        val sha3256 = MessageDigest.getInstance("SHA3-256")
        val hash2 = sha3256.digest(hash1)

        // Step 3: Add version byte to the front of the result of Step 2
        val versionByte = byteArrayOf((version-128).toByte(), SecureRandom(version.toBigInteger().toByteArray()).nextInt().toByte())
        val hash3 = ByteArray(versionByte.size + hash2.size)
        System.arraycopy(versionByte, 0, hash3, 0, versionByte.size)
        System.arraycopy(hash2, 0, hash3, versionByte.size, hash2.size)

        // Step 4: Perform SHA-256 hash on the result of Step 3
        val hash4 = sha256.digest(hash3)

        // Step 5: Perform SHA-256 hash on the result of Step 4
        val hash5 = sha256.digest(hash4)

        // Step 6: Take the first 4 bytes of the result of Step 5, and add them to the end of the result of Step 3
        val checksum = ByteArray(4)
        System.arraycopy(hash5, 0, checksum, 0, 4)
        val hash6 = ByteArray(hash3.size + checksum.size)
        System.arraycopy(hash3, 0, hash6, 0, hash3.size)
        System.arraycopy(checksum, 0, hash6, hash3.size, checksum.size)

        // Step 7: Encode the result of Step 6 using Base58Check encoding
        return Base58.encode(hash6)
    }

    override fun calculateHash(): Hash {
        return Hash(SHA3.hashString("$publicKey $address $time"))
    }

}

class AddressSerializer : JsonSerializer<Address>(){
    override fun serialize(address: Address, jsonGenerator: JsonGenerator, serializerProvider: SerializerProvider) {
        jsonGenerator.writeStartObject()
        jsonGenerator.writeObjectField("publicKey", Base58.encode(address.publicKey.encoded))
        jsonGenerator.writeStringField("address", address.address)
        jsonGenerator.writeEndObject()
    }

}
class AddressDeserializer : JsonDeserializer<Address>() {
    override fun deserialize(jsonParser: JsonParser, deserializationContext: DeserializationContext): Address {
        val node: JsonNode = jsonParser.codec.readTree(jsonParser)
        val keyFactory: KeyFactory = KeyFactory.getInstance("EC")
        val publicKeySpec = X509EncodedKeySpec(Base58.decode(node.get("publicKey").asText()))
        val publicKey: PublicKey = keyFactory.generatePublic(publicKeySpec)
        val address = node.get("address").asText()
        return Address(publicKey, address)
    }
}